package com.soywiz.korge.gradle.bundle

import com.soywiz.korge.gradle.KorgeExtension
import com.soywiz.korge.gradle.gkotlin
import com.soywiz.korge.gradle.util.get
import org.gradle.api.Project
import java.io.File
import java.net.URL

class KorgeBundles(val project: Project) {
    val bundlesDir get() = project.file("bundles").also { it.mkdirs() }
    val logger get() = project.logger
    val bundles = arrayListOf<BundleInfo>()
    data class BundleInfo(
        val path: File,
        val bundleName: String,
        val repositories: List<BundleRepository>,
        val dependencies: List<BundleDependency>
    ) {
        fun dependenciesForSourceSet(sourceSet: String) = dependencies.filter { it.sourceSet == sourceSet }
        fun dependenciesForSourceSet(sourceSet: Set<String>) = dependencies.filter { it.sourceSet in sourceSet }
    }
    data class BundleRepository(val url: String)
    data class BundleDependency(val sourceSet: String, val artifactPath: String)

    @JvmOverloads
    fun bundle(zipFile: File, baseName: String? = null) {
        val bundleName = baseName ?: zipFile.name.removeSuffix(".korgebundle")
        val outputDir = project.file("${project.buildDir}/bundles/$bundleName")
        if (!outputDir.exists()) {
            logger.warn("KorGE.bundle: Unzipping $zipFile...")
            project.sync {
                it.from(if (zipFile.isDirectory) project.fileTree(zipFile) else project.zipTree(zipFile))
                it.into(outputDir)
            }
        } else {
            logger.info("KorGE.bundle: Already unzipped $zipFile")
        }

        val repositories = arrayListOf<BundleRepository>()
        val dependencies = arrayListOf<BundleDependency>()
        val dependenciesTxtFile = File(outputDir, "dependencies.txt")
        if (dependenciesTxtFile.exists()) {
            for (rline in dependenciesTxtFile.readLines()) {
                val line = rline.trim()
                if (line.startsWith("#")) continue
                val (key, value) = line.split(":", limit = 2).map { it.trim() }.takeIf { it.size >= 2 } ?: continue
                if (key == "repository") {
                    repositories.add(BundleRepository(value))
                } else {
                    dependencies.add(BundleDependency(key, value))
                }
            }
        }

        bundles += BundleInfo(
            path = outputDir,
            bundleName = bundleName,
            repositories = repositories,
            dependencies = dependencies,
        )

        logger.info("KorGE.bundle: $outputDir")
        project.afterEvaluate {
            for (target in project.gkotlin.targets) {
                logger.info("  target: $target")
                target.compilations.all { compilation ->
                    logger.info("    compilation: $compilation")
                    for (sourceSet in compilation.kotlinSourceSets) {
                        logger.info("      sourceSet: $sourceSet")
                        val kotlinSrc = File(project.buildDir, "bundles/${bundleName}/src/${sourceSet.name}/kotlin")
                        val resourcesSrc = File(project.buildDir, "bundles/${bundleName}/src/${sourceSet.name}/resources")
                        if (kotlinSrc.exists()) {
                            logger.info("        kotlinSrc: $kotlinSrc")
                            sourceSet.kotlin.srcDirs(kotlinSrc)
                        }
                        if (resourcesSrc.exists()) {
                            logger.info("        resourcesSrc: $resourcesSrc")
                            sourceSet.resources.srcDirs(resourcesSrc)
                        }
                    }
                }
            }
        }
        //println(project.gkotlin.metadata().compilations["main"].kotlinSourceSets)
    }

    @JvmOverloads
    fun bundle(url: java.net.URL, baseName: String? = null) {
        val outFile = bundlesDir["${baseName ?: File(url.path).nameWithoutExtension}.korgebundle"]
        if (!outFile.exists()) {
            logger.warn("KorGE.bundle: Downloading $url...")
            outFile.writeBytes(url.readBytes())
        } else {
            logger.info("KorGE.bundle: Already downloaded $url")
        }
        bundle(outFile, baseName)
    }

    @JvmOverloads
    fun bundleGit(repo: String, folder: String = "", ref: String = "master", bundleName: String? = null) {
        val repoURL = URL(repo)
        val packPath = "${repoURL.host}/${repoURL.path}/$ref"
            .replace("\\", "/")
            .trim('/')
            .replace(Regex("/+"), "/")
            .replace(".git", "")
            .replace("/..", "")

        val packDir = File(bundlesDir, packPath)
        if (!File(packDir, ".git").exists()) {
            packDir.mkdirs()
            logger.warn("KorGE.bundle: Git clonning $repo @ $ref...")
            project.exec {
                it.workingDir(packDir)
                it.commandLine("git", "clone", repo, ".")
            }
            project.exec {
                it.workingDir(packDir)
                it.commandLine("git", "reset", "--hard")
            }
            project.exec {
                it.workingDir(packDir)
                it.commandLine("git", "checkout", ref)
            }
        } else {
            logger.info("KorGE.bundle: Already clonned $repo @ $ref")
        }
        bundle(File(packDir, folder), bundleName)
    }

    @JvmOverloads
    fun bundle(uri: String, baseName: String? = null) {
        when {
            uri.contains(".git") -> {
                val parts = uri.split("::", limit = 3)
                bundleGit(parts[0], parts.getOrElse(1) { "" }, parts.getOrElse(2) { "master" }, parts.getOrNull(3))
            }
            uri.startsWith("http://") || uri.startsWith("https://") -> {
                bundle(URL(uri), baseName)
            }
            else -> {
                bundle(project.file(uri), baseName)
            }
        }
    }

    fun getPaths(name: String, resources: Boolean, test: Boolean): Set<File> {
        val lfolder = if (resources) "resources" else "kotlin"
        val lmain = if (test) "Test" else "Main"
        return bundles.flatMap { bundle ->
            listOf(File(bundle.path, "src/${name}$lmain/$lfolder"))
        }.filter { it.isDirectory && it.exists() }.toSet()
    }
}
