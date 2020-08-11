package notadream

import org.apache.logging.log4j.LogManager

fun main(args: Array<String>) {
    val logger = LogManager.getLogger()

    logger.info("${Common.greeting()} with {} args.", args.size)
}