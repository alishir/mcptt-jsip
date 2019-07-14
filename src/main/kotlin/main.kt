import ir.sina.mcptt.Mcptt
import ir.sina.mcptt.MyStackLogger
import org.apache.logging.log4j.LogManager
import java.io.FileInputStream
import java.util.*
import javax.sip.SipFactory
import javax.sip.address.SipURI


fun main() {
    val logger = LogManager.getLogger("MCPTT-MAIN")
    val properties = Properties()
    try {
        val home = System.getProperty("user.home")
        val configPath = ".mcptt/config"
        val propFile = FileInputStream("$home/$configPath")
        properties.load(propFile)
    } catch (e: Exception) {
        logger.warn("Couldn't load config file ~/.mcptt/config")
    }

    properties.setProperty("javax.sip.STACK_NAME", "MCPTT")
    properties.setProperty("javax.sip.IP_ADDRESS", "0.0.0.0")
    properties.setProperty("gov.nist.javax.sip.STACK_LOGGER", MyStackLogger::class.java.name)

    val sipFactory = SipFactory.getInstance()
    val sipStack = sipFactory.createSipStack(properties)

    val udp = sipStack.createListeningPoint("0.0.0.0", 5060, "udp")
    val sipProviderUdp = sipStack.createSipProvider(udp)

    logger.info("scscf address: ${properties.getProperty("mcptt.scscf.ip")}")

    val users = mutableListOf<SipURI>()
    val mcptt = Mcptt(users, sipFactory, sipProviderUdp, properties)
    sipProviderUdp.addSipListener(mcptt)
}