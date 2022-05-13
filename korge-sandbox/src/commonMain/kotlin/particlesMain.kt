import com.soywiz.korge.particle.particleEmitter
import com.soywiz.korge.particle.readParticleEmitter
import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.addUpdater
import com.soywiz.korge.view.container
import com.soywiz.korge.view.position
import com.soywiz.korio.file.std.resourcesVfs

suspend fun Stage.particlesMain() {
    //val emitter = resourcesVfs["particle/demo2.pex"].readParticleEmitter()
    container {
        //scale = 0.05
        scale = 0.5
        val emitter = resourcesVfs["particle/particle.pex"].readParticleEmitter()
        //val emitter = resourcesVfs["particle/particle.pex"].readParticleEmitter()
        //val emitter = resourcesVfs["particle/1/particle.pex"].readParticleEmitter()
        //val particlesView = particleEmitter(emitter, time = 2.seconds).position(views.virtualWidth * 0.5, views.virtualHeight * 0.5)
        val particlesView = particleEmitter(emitter).position(views.virtualWidth * 0.5, views.virtualHeight * 0.5)
        //val particlesView = particleEmitter(emitter).position(0.0, 0.0)

        addUpdater {
            //particlesView.emitterPos = stage.mouseXY
            particlesView.setGlobalXY(stage!!.mouseXY)
            //println(stage!!.mouseXY)
            //particlesView.x = stage.mouseX
            //particlesView.y = stage.mouseY
        }

        //delay(4.seconds)

        println("RESTART")
        particlesView.restart()
    }
}
