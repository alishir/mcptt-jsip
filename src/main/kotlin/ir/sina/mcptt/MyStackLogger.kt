package ir.sina.mcptt

import gov.nist.core.StackLogger
import org.apache.logging.log4j.LogManager
import java.util.*

class MyStackLogger : StackLogger {
    val logger = LogManager.getLogger("JSIP-STACK")
    override fun logInfo(p0: String?) {
        logger.info(p0)
    }

    override fun logError(p0: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun logError(p0: String?, p1: Exception?) {
        logger.error("$p0 with exception $p1")
    }

    override fun disableLogging() {

    }

    override fun isLoggingEnabled(): Boolean {
        return true
    }

    override fun isLoggingEnabled(p0: Int): Boolean {
        return true
    }

    override fun setStackProperties(p0: Properties?) {
    }

    override fun getLoggerName(): String {
        return "JSIP-STACK"
    }

    override fun logWarning(p0: String?) {
        logger.warn(p0)
    }

    override fun logStackTrace() {
    }

    override fun logStackTrace(p0: Int) {

    }

    override fun logTrace(p0: String?) {
        logger.trace(p0)
    }

    override fun enableLogging() {
    }

    override fun getLineCount(): Int {
        return 110
    }

    override fun logException(p0: Throwable?) {
        logger.error("Exception $p0")
    }

    override fun setBuildTimeStamp(p0: String?) {
        logger.info("Build time: $p0")
    }

    override fun logDebug(p0: String?) {
        logger.debug(p0)
    }

    override fun logDebug(p0: String?, p1: Exception?) {
        logger.debug("$p0 with $p1")
    }

    override fun logFatalError(p0: String?) {
        logger.error(p0)
    }
}