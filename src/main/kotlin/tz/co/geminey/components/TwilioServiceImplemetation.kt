package tz.co.geminey.components

import com.twilio.Twilio
import com.twilio.exception.ApiException
import com.twilio.rest.api.v2010.account.ValidationRequest
import com.twilio.rest.verify.v2.service.Verification
import com.twilio.rest.verify.v2.service.VerificationCheck
import com.twilio.type.PhoneNumber
import kotlinx.serialization.Serializable

class TwilioServiceImplementation: TwilioService {

    private val ACCOUNT_SID = "AC3d0afe540aad4fc2910463708f7b2491"
    private val AUTH_TOKEN = "8a45ef3981460c3b5313a364c53e82cb"
    private val VERIFICATION_SID = "VA45532dcca7733654193982f4643ff066"

    fun init() {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN)
    }

    override fun startVerification(phoneNumber: String): VerificationResult{
        return try {
            val verification = Verification.creator(
                VERIFICATION_SID,
                phoneNumber,
                "sms"
            ).create()
            VerificationResult(verification.sid, valid = true, errors = emptyList())
        } catch (e: ApiException){
            println("MESSAGE NOT SENT:: ${e.message}")
            VerificationResult(errors = listOf(e.message), valid = false, id = "")
        }
    }

    override fun checkVerification(phoneNumber: String, code: String): VerificationResult{
       return try {
            val verification = VerificationCheck.creator(VERIFICATION_SID).setCode(code).setTo(phoneNumber).create()
            if ("approved" == verification.status){
              return  VerificationResult(verification.sid, valid = true, errors = emptyList())
            }
           VerificationResult("", valid = false, errors = listOf("Invalid code."))
        } catch (e: ApiException){
           return VerificationResult(errors = listOf(e.message), valid = false, id = "")
        }
    }

    override fun addVerificationCallerID(phoneNumber: String): VerificationResult {
        return try {
            val verification = ValidationRequest.creator(PhoneNumber(phoneNumber)).setFriendlyName(phoneNumber.removePrefix("+")).create()
            VerificationResult(verification.callSid, valid = true, errors = emptyList())
        }catch (e: Exception){
            e.printStackTrace()
            println("MESSAGE NOT SENT:: ${e.message}")
            VerificationResult(errors = listOf(e.message), valid = false, id = "")
        }
    }
}

@Serializable
data class VerificationResult(
    val id: String,
    val errors: List<String?>? = null,
    val valid: Boolean
)