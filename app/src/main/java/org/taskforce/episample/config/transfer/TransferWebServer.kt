package org.taskforce.episample.config.transfer

import fi.iki.elonen.NanoHTTPD

class TransferWebServer(val port: Int = 8080) : NanoHTTPD(port) {

    init {
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
    }

    override fun serve(session: IHTTPSession): Response {
        var msg = "<html><body><h1>Hello server</h1>\n"
        msg += if (session.parameters["username"] == null) {
            "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n"
        } else {
            "<p>Hello, " + session.parameters["username"] + "!</p>"
        }
        msg += "</body></html>\n"
        return NanoHTTPD.newFixedLengthResponse(msg)
    }
}