#pragma clang diagnostic ignored "-Wwritable-strings"
#pragma ide diagnostic ignored "OCUnusedMacroInspection"
#pragma ide diagnostic ignored "UnusedValue"
#pragma ide diagnostic ignored "modernize-use-nullptr"
#pragma ide diagnostic ignored "cppcoreguidelines-narrowing-conversions"

#define SECURITY_WIN32 1

#include <winsock2.h>
#include <sspi.h>
#include <schannel.h>
#include <cstdio>
#include <commctrl.h>
#include <cstring>
#include <tchar.h>
#include <cassert>
#include <ntdef.h>
#include <cmath>
#include <algorithm>

#define SET_SSL_BUFFER(buffer, type, count, pv) { buffer.BufferType = type; buffer.cbBuffer = count; buffer.pvBuffer = pv; }
#define SET_SSL_BUFFERS(sbin, bufsi, count) { sbin.ulVersion = SECBUFFER_VERSION; sbin.pBuffers = bufsi; sbin.cBuffers = count; }

typedef struct {
    long long int read;
    long long int written;
    int allocatedSize;
    unsigned char *ptr;
} GrowableDeque;

void hexdumpsingleline(char *c, int size) {
    unsigned char *data = (unsigned char *)c;
    for (int i = 0; i < size; i++) {
        printf("%02X", data[i]);
    }
    printf("\n");
}

void hexdump(char *c, int size) {
    unsigned char *data = (unsigned char *)c;
    for (int m = 0; m < size; m += 16) {
        int m2 = m + 16;
        for (int i = m; i < m2; i++) if (i < size) printf("%02X ", data[i]); else printf("   ");
        for (int i = m; i < m2; i++) if (i < size) printf("%c", isalnum(data[i]) ? data[i] : '.'); else printf(" ");
        printf("\n");
    }
}

GrowableDeque *GD_alloc(int capacity) {
    GrowableDeque *out = (GrowableDeque *)malloc(sizeof(GrowableDeque));
    memset(out, 0, sizeof(*out));
    out->allocatedSize = capacity;
    out->ptr = (unsigned char *)malloc(capacity);
    return out;
}

void GD_free(GrowableDeque *gd) {
    if (gd->ptr != NULL) {
        free(gd->ptr);
        memset(gd, 0, sizeof(GrowableDeque));
    }
    free(gd);
}

void GD_free_safe(GrowableDeque **gd) {
    GD_free(*gd);
    *gd = NULL;
}

long long int GD_get_pending_read(GrowableDeque *gd) {
    return gd->written - gd->read;
}

void GD_ensure_append(GrowableDeque *gd, int count) {
    long long int pendingRead = GD_get_pending_read(gd);
    if (pendingRead >= gd->allocatedSize) {
        int oldSize = gd->allocatedSize;
        int newSize = std::max(gd->allocatedSize + count, gd->allocatedSize * 3);
        unsigned char *oldPtr = gd->ptr;
        unsigned char *newPtr = (unsigned char *)malloc(newSize);
        memset(newPtr, 0, newSize);
        gd->allocatedSize = newSize;
        gd->ptr = newPtr;

        for (int n = 0; n < pendingRead; n++) {
            newPtr[(gd->read + n) % newSize] = oldPtr[(gd->read + n) % oldSize];
        }

        free(oldPtr);
    }
}

void GD_append_byte(GrowableDeque *gd, char data) {
    GD_ensure_append(gd, 1);
    gd->ptr[gd->written++ % gd->allocatedSize] = data;
}

int GD_read_byte(GrowableDeque *gd) {
    if (gd->read >= gd->written) return -1;
    return gd->ptr[gd->read++ % gd->allocatedSize];
}

int GD_peek_byte(GrowableDeque *gd, int offset) {
    if ((gd->read + offset) >= gd->written) return -1;
    return gd->ptr[(gd->read + offset) % gd->allocatedSize];
}

// @TODO: Optimize
void GD_append(GrowableDeque *gd, char *data, int count) {
    GD_ensure_append(gd, count);
    for (int n = 0; n < count; n++) GD_append_byte(gd, data[n]);
}

// @TODO: Optimize
int GD_read(GrowableDeque *gd, char *data, int count) {
    for (int n = 0; n < count; n++) {
        int byte = GD_read_byte(gd);
        if (byte < 0) return n;
        data[n] = byte;
    }
    return count;
}

int GD_read_skip(GrowableDeque *gd, int count) {
    gd->read += std::min(GD_get_pending_read(gd), (long long int)count);
}

// @TODO: Optimize
int GD_peek(GrowableDeque *gd, int offset, char *data, int count) {
    for (int n = 0; n < count; n++) {
        int byte = GD_peek_byte(gd, offset + n);
        if (byte < 0) return n;
        data[n] = byte;
    }
    return count;
}

// @TODO: Optimize
int GD_copy(GrowableDeque *src, GrowableDeque *dst, int count) {
    GD_ensure_append(dst, count);
    for (int n = 0; n < count; n++) {
        int data = GD_read_byte(src);
        if (data < 0) break;
        GD_append_byte(dst, data);
    }
}

// @TODO: Optimize
void GD_prepend(GrowableDeque *gd, char *data, int count) {
    int pendingRead = GD_get_pending_read(gd);
    int total = pendingRead + count;
    char *newBuffer = (char *)(malloc(total));
    memcpy(newBuffer, data, count);
    GD_read(gd, newBuffer + count, pendingRead);
    gd->read = 0;
    gd->written = total;
    gd->allocatedSize = total;
    if (gd->ptr != NULL) free(gd->ptr);
    gd->ptr = (unsigned char *)newBuffer;
}

void GD_clear(GrowableDeque *gd) {
    gd->read = 0;
    gd->written = 0;
    if (gd->ptr != NULL) memset(gd->ptr, 0, gd->allocatedSize);
}

void GD_dump(GrowableDeque *gd) {
    int available = GD_get_pending_read(gd);
    printf("GD: ");
    for (int n = 0; n < available; n++) printf("%c", GD_peek_byte(gd, n));
    printf("\n");
    printf("GD: ");
    for (int n = 0; n < available; n++) printf("%02X", GD_peek_byte(gd, n));
    printf("\n");
}

class SSL_SOCKET {
public:

    SSL_SOCKET();

    void setDestinationName(TCHAR *n);

    ~SSL_SOCKET();

    int out_queue(char *b, int sz) const;
    int out_dequeue(char *b, int sz) const;
    int out_get_pending() const;

    int in_queue(char *b, int sz) const;
    int in_dequeue(char *b, int sz) const;

    int process();

    int writeToSend(char *b, int sz) const;
    int writeReceived(char *b, int sz) const;
    void close();

private:
    GrowableDeque *in_buffer;
    GrowableDeque *out_buffer;

    GrowableDeque *to_write_buffer;
    GrowableDeque *received_buffer;

    HCERTSTORE hCS;
    SCHANNEL_CRED m_SchannelCred;
    CredHandle hCred;
    CtxtHandle hCtx;
    TCHAR dn[1000];
    SecBufferDesc sbin;
    SecBufferDesc sbout;
    bool InitContext;
    bool clientSendDisconnect = false;

    bool mustNegotiate = true;
};

SSL_SOCKET::SSL_SOCKET() {
    hCS = 0;
    hCred.dwLower = 0;
    hCred.dwUpper = 0;
    hCtx.dwLower = 0;
    hCtx.dwUpper = 0;
    memset(dn, 0, 1000 * sizeof(TCHAR));
    InitContext = false;
    mustNegotiate = true;
    memset(&m_SchannelCred, 0, sizeof(m_SchannelCred));
    m_SchannelCred.dwVersion = SCHANNEL_CRED_VERSION;
    m_SchannelCred.dwFlags = SCH_CRED_NO_DEFAULT_CREDS | SCH_CRED_NO_SYSTEM_MAPPER | SCH_CRED_REVOCATION_CHECK_CHAIN;
    SECURITY_STATUS ss = AcquireCredentialsHandle(0, SCHANNEL_NAME, SECPKG_CRED_OUTBOUND, 0, NULL, 0, 0, &hCred, 0);
    assert(!FAILED(ss));
    out_buffer = GD_alloc(64);
    in_buffer = GD_alloc(64);
    to_write_buffer = GD_alloc(64);
    received_buffer = GD_alloc(64);
    clientSendDisconnect = false;
}

void SSL_SOCKET::close() {
    clientSendDisconnect = true;
}

SSL_SOCKET::~SSL_SOCKET() {
    if (hCtx.dwLower || hCtx.dwLower) DeleteSecurityContext(&hCtx);
    if (hCred.dwLower || hCred.dwLower) FreeCredentialHandle;
    if (hCS) CertCloseStore(hCS, 0);
    GD_free_safe(&out_buffer);
    GD_free_safe(&in_buffer);
    GD_free_safe(&to_write_buffer);
    GD_free_safe(&received_buffer);
    hCS = 0;
}


void SSL_SOCKET::setDestinationName(TCHAR *n) {
    _tcscpy(dn, n);
}

int SSL_SOCKET::out_queue(char *b, int sz) const { GD_append(out_buffer, b, sz); return sz; }
int SSL_SOCKET::out_get_pending() const { return GD_get_pending_read(out_buffer); }
int SSL_SOCKET::out_dequeue(char *out, int size) const { return GD_read(out_buffer, out, size); }

int SSL_SOCKET::in_queue(char *b, int sz) const { GD_append(in_buffer, b, sz); return sz; }
int SSL_SOCKET::in_dequeue(char *out, int size) const { return GD_read(in_buffer, out, size); }

int SSL_SOCKET::writeReceived(char *b, int sz) const {
    assert(sz >= 0);
    GD_append(received_buffer, b, sz);
    return sz;
}

int SSL_SOCKET::writeToSend(char *b, int sz) const {
    assert(sz >= 0);
    GD_append(to_write_buffer, b, sz);
    return sz;
}

int SSL_SOCKET::process() {
    SecPkgContext_StreamSizes sizes;
    SecBuffer Buffers[5] = {0};

    // Client negotiate
    if (mustNegotiate || InitContext == 0) {
        SECURITY_STATUS ss = SEC_I_CONTINUE_NEEDED;
        char *t = (char *)malloc(0x11000);
        SecBuffer bufsi[2];
        SecBuffer bufso[2];
        bool failed = false;

        printf("NEGOTIATION START: mustNegotiate=%d, InitContext=%d\n", mustNegotiate, InitContext);

        // Loop using InitializeSecurityContext until success
        while (true) {
            if (ss != SEC_I_CONTINUE_NEEDED && ss != SEC_E_INCOMPLETE_MESSAGE && ss != SEC_I_INCOMPLETE_CREDENTIALS) {
                break;
            }

            int pt = 0;

            if (InitContext == 0) {
                // Initialize sbout
                SET_SSL_BUFFER(bufso[0], SECBUFFER_TOKEN, 0, 0)
                SET_SSL_BUFFERS(sbout, bufso, 1);
            } else {
                pt = GD_read(received_buffer, t, GD_get_pending_read(received_buffer));

                if (pt == 0) return -1;

                SET_SSL_BUFFER(bufsi[0], SECBUFFER_TOKEN, pt, t)
                SET_SSL_BUFFER(bufsi[1], SECBUFFER_EMPTY, 0, 0)
                SET_SSL_BUFFERS(sbin, bufsi, 2);

                SET_SSL_BUFFER(bufso[0], SECBUFFER_TOKEN, 0, 0)
                SET_SSL_BUFFER(bufso[1], SECBUFFER_EMPTY, 0, 0)
                SET_SSL_BUFFERS(sbout, bufso, 2);
            }

            DWORD dwSSPIOutFlags = 0;

            ss = InitializeSecurityContext(
                &hCred, // phCredential
                InitContext ? &hCtx : NULL, // phContext
                dn, // pszTargetName
                ISC_REQ_SEQUENCE_DETECT | ISC_REQ_REPLAY_DETECT | ISC_REQ_CONFIDENTIALITY | ISC_RET_EXTENDED_ERROR | ISC_REQ_ALLOCATE_MEMORY | ISC_REQ_STREAM | ISC_REQ_MANUAL_CRED_VALIDATION, // fContextReq
                0, // Reserved1
                0, // TargetDataRep
                InitContext ? &sbin : NULL, // pInput
                0, // Reserved2
                InitContext ? NULL : &hCtx, // phNewContext
                &sbout, // pOutput
                &dwSSPIOutFlags, // pfContextAttr
                NULL // ptsExpiry
            );
            printf("Process: InitializeSecurityContext. ss=0x%08x, pt=%d\n", ss, pt);

            for (int n = 0; n < 2; n++) {
                SecBuffer *buffer = &bufso[n];
                if (buffer && buffer->BufferType == SECBUFFER_EXTRA) {
                    GD_prepend(received_buffer, (char *)buffer->pvBuffer, buffer->cbBuffer);
                }
            }

            if (ss == SEC_E_INCOMPLETE_MESSAGE) {
                printf("Negotiate: ss == SEC_E_INCOMPLETE_MESSAGE\n");
                failed = true;
                break;
            }

            pt = 0;

            if (FAILED(ss)) {
                printf("Negotiate: FAILED(ss)\n");
                failed = true;
                break;
            }

            if (InitContext == 0 && ss != SEC_I_CONTINUE_NEEDED) {
                printf("Negotiate: SEC_I_CONTINUE_NEEDED\n");
                failed = true;
                break;
            }

            // Pass data to the remote site
            out_queue((char *) bufso[0].pvBuffer, bufso[0].cbBuffer);
            FreeContextBuffer(bufso[0].pvBuffer);
            //send_pending();

            if (!InitContext) {
                printf("Negotiate: !InitContext -> InitContext\n");
                InitContext = true;
                failed = true;
                break;
            }

            if (ss == S_OK) {
                printf("Negotiate: ss == S_OK -> mustNegotiate = false;\n");
                mustNegotiate = false;
                InitContext = 1;
                break; // wow!!
            }
        }

        free(t);

        if (failed) {
            return -1;
        }
    }

    SECURITY_STATUS ss = QueryContextAttributes(&hCtx, SECPKG_ATTR_STREAM_SIZES, &sizes);
    if (FAILED(ss)) {
        printf("QueryContextAttributes.failed: 0x%08x\n", ss);
        return -1;
    }

    // Decode read pending
    {
        char *mmsg = (char *)malloc(sizes.cbMaximumMessage);

        while (GD_get_pending_read(received_buffer) > 0) {
            int rval = GD_read(received_buffer, mmsg, sizes.cbMaximumMessage);
            if (rval == 0 || rval == -1) {
                printf("NOT ENOUGH DATA!\n");
                break;
            }

            SET_SSL_BUFFER(Buffers[0], SECBUFFER_DATA, rval, mmsg)
            SET_SSL_BUFFER(Buffers[1], SECBUFFER_EMPTY, 0, NULL)
            SET_SSL_BUFFER(Buffers[2], SECBUFFER_EMPTY, 0, NULL)
            SET_SSL_BUFFER(Buffers[3], SECBUFFER_EMPTY, 0, NULL)
            SET_SSL_BUFFERS(sbin, Buffers, 4)

            ss = DecryptMessage(&hCtx, &sbin, 0, NULL);

            printf("DecryptMessage.ss = 0x%08X, rval=%d\n", ss, rval);

            if (ss == SEC_E_INCOMPLETE_MESSAGE) {
                // Must feed more data
                printf("DecryptMessage requires more data!\n");
                break;
            }

            if (ss != SEC_E_OK && ss != SEC_I_RENEGOTIATE && ss != SEC_I_CONTEXT_EXPIRED) {
                printf("Process mustNegotiate = true\n");
                //mustNegotiate = true;
                //mustNegotiate = true;
                //InitContext = 0;
                break;
            }

            for (int i = 0; i < 4; i++) {
                SecBuffer *buffer = &Buffers[i];
                if (buffer && buffer->BufferType == SECBUFFER_DATA) {
                    in_queue((char *)buffer->pvBuffer, buffer->cbBuffer);
                    printf("DECRYPTED OUTPUT(%d)\n", buffer->cbBuffer);
                }
                if (buffer && buffer->BufferType == SECBUFFER_EXTRA) {
                    GD_prepend(received_buffer, (char *)buffer->pvBuffer, buffer->cbBuffer);
                }
            }

            if (ss == SEC_I_RENEGOTIATE) {
                mustNegotiate = true;
                if (FAILED(ss)) {
                    break;
                }
            }
        }

        free(mmsg);
    }
    // Encrypt and write pending
    {
        char *data = (char *) malloc(sizes.cbMaximumMessage);
        char *mhdr = (char *) malloc(sizes.cbHeader);
        char *mtrl = (char *) malloc(sizes.cbTrailer);

        while (GD_get_pending_read(to_write_buffer) > 0) {
            int dataSize = GD_read(to_write_buffer, data, sizes.cbMaximumMessage);

            SET_SSL_BUFFER(Buffers[0], SECBUFFER_STREAM_HEADER, sizes.cbHeader, mhdr)
            SET_SSL_BUFFER(Buffers[1], SECBUFFER_DATA, dataSize, data)
            SET_SSL_BUFFER(Buffers[2], SECBUFFER_STREAM_TRAILER, sizes.cbTrailer, mtrl)
            SET_SSL_BUFFER(Buffers[3], SECBUFFER_EMPTY, 0, 0)
            SET_SSL_BUFFERS(sbin, Buffers, 4)

            ss = EncryptMessage(&hCtx, 0, &sbin, 0);
            if (FAILED(ss)) {
                fprintf(stderr, "ERROR in EncryptMessage\n");
                break;
            }

            // Send this message
            for (int n = 0; n < 3; n++) {
                out_queue((char *) Buffers[n].pvBuffer, Buffers[n].cbBuffer);
            }
        }

        free(mtrl);
        free(mhdr);
        free(data);
    }
    // Encrypt and write client wants to disconnect
    if (clientSendDisconnect) {
        clientSendDisconnect = false;
        // Client wants to disconnect

        SECURITY_STATUS ss;
        SecBuffer OutBuffers[1];
        DWORD dwType = SCHANNEL_SHUTDOWN;

        SET_SSL_BUFFER(OutBuffers[0], SECBUFFER_TOKEN, sizeof(dwType), &dwType)
        SET_SSL_BUFFERS(sbout, OutBuffers, 1)

        while (true) {
            ss = ApplyControlToken(&hCtx, &sbout);
            if (FAILED(ss)) {
                return -1;
            }

            DWORD dwSSPIFlags;
            DWORD dwSSPIOutFlags;
            dwSSPIFlags =
                    ISC_REQ_SEQUENCE_DETECT | ISC_REQ_REPLAY_DETECT | ISC_REQ_CONFIDENTIALITY | ISC_RET_EXTENDED_ERROR |
                    ISC_REQ_ALLOCATE_MEMORY | ISC_REQ_STREAM;

            SET_SSL_BUFFER(OutBuffers[0], SECBUFFER_TOKEN, 0, 0)
            SET_SSL_BUFFERS(sbout, OutBuffers, 1)

            ss = InitializeSecurityContext(
                &hCred, &hCtx, NULL, dwSSPIFlags, 0, SECURITY_NATIVE_DREP, NULL, 0, &hCtx,
                &sbout, &dwSSPIOutFlags, 0
            );
            if (FAILED(ss)) {
                return -1;
            }

            PBYTE pbMessage;
            DWORD cbMessage;
            pbMessage = (BYTE *) (OutBuffers[0].pvBuffer);
            cbMessage = OutBuffers[0].cbBuffer;

            if (pbMessage != NULL && cbMessage != 0) {
                out_queue((char *) pbMessage, cbMessage);
                FreeContextBuffer(pbMessage);
            }
            break;
        }
    }

    return 0;
}

#define REQUEST_HOST "www.google.es"
//#define REQUEST_HOST "php.net"

int main() {
    int BUFFER_SIZE = 0x10000;

    int argc = 3;
    char *argv[] = {"program", REQUEST_HOST, "443"};
    SOCKET s;
    SSL_SOCKET *sx = 0;
    sockaddr_in dA, aa;
    int slen = sizeof(sockaddr_in);

    InitCommonControls();
    OleInitialize(0);
    WSADATA wData;
    WSAStartup(MAKEWORD(2, 2), &wData);
    printf("Tel 2.0 , Chourdakis Michael\r\n");
    if (argc < 2) {
        printf("Usage 1 : TEL <ip> <port>\r\n");
        printf("Usage 2 : TEL <port>\r\n");
        printf("Use * before the port to initiate a SSL session.\r\n");
        return 1;
    }
    char port[100] = {0};
    strcpy(port, argv[2]);

    printf("Mode 1 - connect to %s:%s...\r\n", argv[1], argv[2]);
    hostent *hp;

    memset(&dA, 0, sizeof(dA));
    dA.sin_family = AF_INET;
    unsigned long inaddr = inet_addr(argv[1]);
    if (inaddr != INADDR_NONE) {
        memcpy(&dA.sin_addr, &inaddr, sizeof(inaddr));
    } else {
        hp = gethostbyname(argv[1]);
        if (!hp) {
            printf("--- Remote system not found !\r\n");
            return 2;
        }
        memcpy(&dA.sin_addr, hp->h_addr, hp->h_length);
    }
    dA.sin_port = htons(atoi(port));
    s = socket(AF_INET, SOCK_STREAM, 0);
    if (connect(s, (sockaddr *) &dA, slen) < 0) {
        printf("--- Cannot connect !\r\n");
        return 3;
    }

    getpeername(s, (sockaddr *) &aa, &slen);

    //{ u_long value = 1; ioctlsocket(s, FIONBIO, &value); }

    printf("OK , connected with %s:%u...\r\n\r\n", inet_ntoa(aa.sin_addr), ntohs(aa.sin_port));
    sx = new SSL_SOCKET();
    sx->setDestinationName(argv[1]);

    char *message = "GET / HTTP/1.1\r\nHost: " REQUEST_HOST "\r\nConnection: close\r\n\r\n";
    printf("%s\n", message);

    sx->writeToSend(message, strlen(message));

    int isAlive = true;

    char *c = (char *) malloc(BUFFER_SIZE);

    while (isAlive) {
        memset(c, 0, BUFFER_SIZE);

        //printf("STEP[0]\n");
        sx->process();
        //printf("STEP[1]\n");

        do {
            int toWriteCount = sx->out_dequeue(c, BUFFER_SIZE);
            if (toWriteCount > 0) {
                int pos = 0;
                while (pos < toWriteCount) {
                    int sent = send(s, c + pos, toWriteCount, 0);
                    pos += sent;
                }
                //send(s, c, toWriteCount, 0);
            } else {
                break;
            }
        } while (true);

        //printf("STEP[2]\n");

        {
            int readCount = recv(s, c, BUFFER_SIZE, 0);
            //printf("-->%d, errno=%d\n", readCount, errno);
            if (readCount == 0) isAlive = false; // only for blocking sockets
            if (readCount < 0) isAlive = false;
            if (readCount > 0) {
                sx->writeReceived(c, readCount);
            }
        }

        //printf("STEP[3]\n");

        do {
            int readCount = sx->in_dequeue(c, BUFFER_SIZE - 1);
            if (readCount > 0) {
                c[readCount] = 0;
                printf("####### readCount=%d\n", readCount);
                printf("%s\n", c);
            } else {
                break;
            }
        } while (true);

        //printf("STEP[4]\n");
    }

    free(c);

    return 0;
}
