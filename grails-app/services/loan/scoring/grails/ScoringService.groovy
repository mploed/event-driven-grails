package loan.scoring.grails

import com.budjb.rabbitmq.publisher.RabbitMessageProperties
import com.budjb.rabbitmq.publisher.RabbitMessagePublisher
import grails.transaction.Transactional
import groovy.json.JsonBuilder
import reactor.spring.context.annotation.Consumer
import reactor.spring.context.annotation.Selector

@Transactional
@Consumer
class ScoringService {

    RabbitMessagePublisher rabbitMessagePublisher

    @Selector("financialSituationEntered")
    def receiveInternalMessage(def result) {
        scoreFinancialSituation(result.financialSituation, result.creditDetails.term, result.creditDetails.amount, result.applicationNumber)
    }

    def scoreFinancialSituation(def financialSituation, def term, def amount, def applicationNumber) {
        def sumOutgoings = financialSituation.outgoings.rent + financialSituation.outgoings.loanRepayments + financialSituation.outgoings.costOfLiving

        def sumEarnings = financialSituation.earnings.income + financialSituation.earnings.childBenefit + financialSituation.earnings.rentalIncome

        def monthlyCostOfCredit = term / amount

        ScoringEvent event = new ScoringEvent(eventId: UUID.randomUUID().toString(), applicationNumber: applicationNumber, creationTime: new Date())
        def jsonEvent = new JsonBuilder(event).toString()
        def properties = new RabbitMessageProperties(contentType: "application/json", body: jsonEvent)

        if(sumEarnings - sumOutgoings - monthlyCostOfCredit > 0) {
            properties.setExchange("ScoringPositiveTopic")
            rabbitMessagePublisher.send(properties)
        } else {
            properties.setExchange("ScoringNegativeTopic")
            rabbitMessagePublisher.send(properties)
        }

    }
}
