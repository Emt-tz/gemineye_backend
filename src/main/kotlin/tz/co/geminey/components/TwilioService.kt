package tz.co.geminey.components

interface TwilioService {

    companion object{
        fun create(): TwilioServiceImplementation{
            val twilio = TwilioServiceImplementation()
            twilio.init()
            return twilio
        }
    }

    fun startVerification(phoneNumber: String): VerificationResult

    fun checkVerification(phoneNumber: String, code: String): VerificationResult

    fun addVerificationCallerID(phoneNumber: String): VerificationResult
}
