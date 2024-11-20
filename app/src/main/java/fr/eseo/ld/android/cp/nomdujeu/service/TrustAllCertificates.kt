package fr.eseo.ld.android.cp.nomdujeu.service

import javax.net.ssl.X509TrustManager

// Certificates accepted for websocket (to connect if ssl isn't valide now)
object TrustAllCertificates : X509TrustManager {
    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>?, authType: String?) {}
    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>?, authType: String?) {}
    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate>? = null
}
