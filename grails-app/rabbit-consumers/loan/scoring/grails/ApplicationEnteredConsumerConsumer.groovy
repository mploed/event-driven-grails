package loan.scoring.grails

import com.budjb.rabbitmq.consumer.MessageContext
import grails.events.Events

class ApplicationEnteredConsumerConsumer implements Events{

    static rabbitConfig = [
        queue: "GrailsScoring.CreditApplicationEntered"
    ]

    /**
     * Handle an incoming RabbitMQ message.
     *
     * @param body    The converted body of the incoming message.
     * @param context Properties of the incoming message.
     * @return
     */
    def handleMessage(def body, MessageContext messageContext) {
        def slurper = new groovy.json.JsonSlurper()
        def result = slurper.parseText(body)

        notify "financialSituationEntered", result
    }
}
