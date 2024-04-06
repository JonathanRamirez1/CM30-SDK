package com.jonathan.cm30sdk.ui.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ciontek.ciontekposservice.aidlv2.readcard.CheckCardCallbackV2
import com.jonathan.cm30sdk.BuildConfig
import com.jonathan.cm30sdk.databinding.ActivityMainBinding
import com.jonathan.cm30sdk.ui.viewmodel.PaymentViewModel
import com.jonathan.cm30sdk.ui.viewmodel.PaymentViewModelFactory
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentIntentResult
import com.stripe.android.Stripe
import com.stripe.android.model.CardParams
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodCreateParams
import com.stripe.android.model.StripeIntent
import pos.paylib.posPayKernel
import java.io.FileDescriptor

class MainActivity : AppCompatActivity(), IBinder {

    private lateinit var binding: ActivityMainBinding
    private lateinit var payKernel: posPayKernel
    private lateinit var stripe: Stripe
    private lateinit var paymentViewModel: PaymentViewModel
    private lateinit var progressDialog: ProgressDialog
    private val stripeApiKey = BuildConfig.STRIPE_API_KEY
    private var mPaymentMethodId = ""
    private var mClientSecret = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        paymentViewModel = ViewModelProvider(this, PaymentViewModelFactory())[PaymentViewModel::class.java]
        setContentView(binding.root)
        observeViewModel()
        initializePaySDK()
        stripe = Stripe(applicationContext, stripeApiKey)
        processPaymentManual()
        confirmPayment()
        showProgressDialog()
    }

    private fun observeViewModel() {
        paymentViewModel.paymentIntentResult.observe(this) { result ->
            result.onSuccess { paymentIntentResponse ->
                mClientSecret = paymentIntentResponse.clientSecret
            }.onFailure { error ->
                Log.e("MainActivity", "Error during payment process: ${error.message}")
            }
            progressDialog.dismiss()
        }
    }

    private fun showProgressDialog(): ProgressDialog {
        progressDialog = ProgressDialog(this).apply {
            setMessage("Procesando pago...")
            setCancelable(false)
        }
        return progressDialog
    }

    private fun initializePaySDK() {
        payKernel = posPayKernel.getInstance()
        binding.buttonInitScanCard.setOnClickListener {
            payKernel.initPaySDK(this, object : posPayKernel.ConnectCallback {
                override fun onConnectPaySDK() {
                    Log.d("MainActivity", "SDK conectado correctamente")
                    readCard()
                }

                override fun onDisconnectPaySDK() {
                    Log.d("MainActivity", "SDK desconectado")
                }
            })
        }
    }

    private fun processPaymentManual() {
        binding.apply {
            buttonProcessPayment.setOnClickListener {
                val paymentMethodCreateParams = cardInputWidget.paymentMethodCreateParams
                val amount = editTextAmount.text.toString()
                val currency = editTextCurrency.text.toString()
                if (paymentMethodCreateParams != null && amount.isNotEmpty() && currency.isNotEmpty()) {
                    progressDialog.show()
                    val callback = object : ApiResultCallback<PaymentMethod> {
                        override fun onSuccess(result: PaymentMethod) {
                            try {
                                Log.d(
                                    "PaymentMethodSuccess",
                                    "Successfully created payment method: ${result.id}"
                                )
                                mPaymentMethodId = result.id ?: ""
                                paymentViewModel.createPaymentIntentAndProceed(
                                    amount.toLong(),
                                    currency
                                )
                                buttonConfirmPay.visibility = View.VISIBLE
                            } catch (exception: Exception) {
                                Log.d("PaymentMethodException", "Error payment method: ${exception.message}")
                            }
                        }

                        override fun onError(e: Exception) {
                            Log.d(
                                "PaymentMethodSuccess",
                                "Error creating payment method: ${e.message}"
                            )
                        }
                    }
                    stripe.createPaymentMethod(paymentMethodCreateParams, null, null, callback)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Llene la informacion de la tarjeta",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun confirmPayment() {
        binding.buttonConfirmPay.setOnClickListener {
            if (mPaymentMethodId.isNotEmpty() && mClientSecret.isNotEmpty()) {
                stripe.confirmPayment(
                    this,
                    ConfirmPaymentIntentParams.createWithPaymentMethodId(
                        mPaymentMethodId,
                        mClientSecret
                    )
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        stripe.onPaymentResult(requestCode, data, object : ApiResultCallback<PaymentIntentResult> {
            override fun onSuccess(result: PaymentIntentResult) {
                val paymentIntent = result.intent
                val status = paymentIntent.status
                if (status == StripeIntent.Status.Succeeded) {
                    Log.d("PaymentSuccess", "Payment succeeded: $paymentIntent")
                    Toast.makeText(this@MainActivity, "Pago exitoso!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(e: Exception) {
                Log.e("PaymentError", "Payment failed", e)
                Toast.makeText(this@MainActivity, "Pago fallido!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        payKernel.destroyPaySDK()
    }

    private fun readCard() {
        payKernel.mReadCardOptV2?.let { readCardOptV2 ->
            val cardType = 1
            val timeout = 120

            readCardOptV2.checkCard(cardType, object : CheckCardCallbackV2 {
                override fun asBinder(): IBinder {
                    // Este método es específico para la comunicación entre procesos en Android
                    return this@MainActivity
                }

                override fun findMagCard(infoCard: Bundle?) {
                    // Manejar la detección de una tarjeta de banda magnética
                    val amount = binding.editTextAmount.text.toString()
                    val currency = binding.editTextCurrency.text.toString()

                    if (amount.isNotEmpty() && currency.isNotEmpty()) {
                        infoCard?.let { bundle ->
                            val cardType = bundle.getInt("cardType")
                            val track1 = bundle.getString("TRACK1")
                            val track2 = bundle.getString("TRACK2")
                            val track3 = bundle.getString("TRACK3")
                            val pan = bundle.getString("pan")
                            val name = bundle.getString("name")
                            val expireDate = bundle.getString("expire")
                            val serviceCode = bundle.getString("servicecode")

                            val expireMonth = expireDate?.substring(0, 2)?.toIntOrNull()
                            val expireYear = expireDate?.substring(2, 4)?.toIntOrNull()?.let { it + 2000 }

                            val paymentMethodCreateParams = PaymentMethodCreateParams.createCard(
                                CardParams(
                                    number = pan ?: "",
                                    expMonth = expireMonth ?: 0,
                                    expYear = expireYear ?: 0,
                                    cvc = serviceCode
                                )
                            )

                            val callback = object : ApiResultCallback<PaymentMethod> {
                                override fun onSuccess(result: PaymentMethod) {
                                    Log.d("PaymentMethodSuccess", "Exitoso, se ha creado el payment method: ${result.id}")
                                    mPaymentMethodId = result.id ?: ""
                                    paymentViewModel.createPaymentIntentAndProceed(amount.toLong(), currency)
                                }

                                override fun onError(e: Exception) {
                                    Log.d("PaymentMethodSuccess", "Error creando payment method: ${e.message}")
                                }
                            }
                            stripe.createPaymentMethod(paymentMethodCreateParams, null, null, callback)
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Llene la informacion de la tarjeta", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun findICCard(p0: String?) {
                    // Manejar la detección de una tarjeta IC (tarjeta con chip)
                    Log.d("MainActivity", "Tarjeta IC encontrada: $p0")
                }

                override fun findRFCard(p0: String?) {
                    // Manejar la detección de una tarjeta RF (NFC)
                    Log.d("MainActivity", "Tarjeta RF (NFC) encontrada: $p0")
                }

                override fun onError(p0: Int, p1: String?) {
                    // Manejar errores en la lectura de la tarjeta
                    Log.e("MainActivity", "Error al leer la tarjeta: Código $p0, Mensaje $p1")
                }

                override fun findICCardEx(p0: Bundle?) {
                    // Manejar la detección de una tarjeta IC con información extendida
                    Log.d("MainActivity", "Tarjeta IC (extendida) encontrada: $p0")
                }

                override fun findRFCardEx(p0: Bundle?) {
                    // Manejar la detección de una tarjeta RF (NFC) con información extendida
                    Log.d("MainActivity", "Tarjeta RF (NFC, extendida) encontrada: $p0")
                }

                override fun onErrorEx(p0: Bundle?) {
                    // Manejar errores en la lectura de la tarjeta con información extendida
                    Log.e("MainActivity", "Error extendido al leer la tarjeta: $p0")
                }
            }, timeout)
        } ?: run {
            Log.e("MainActivity", "Error: mReadCardOptV2 es null")
        }
    }

    override fun getInterfaceDescriptor(): String? {
        TODO("Not yet implemented")
    }

    override fun pingBinder(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isBinderAlive(): Boolean {
        TODO("Not yet implemented")
    }

    override fun queryLocalInterface(p0: String): IInterface? {
        TODO("Not yet implemented")
    }

    override fun dump(p0: FileDescriptor, p1: Array<out String>?) {
        TODO("Not yet implemented")
    }

    override fun dumpAsync(p0: FileDescriptor, p1: Array<out String>?) {
        TODO("Not yet implemented")
    }

    override fun transact(p0: Int, p1: Parcel, p2: Parcel?, p3: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun linkToDeath(p0: IBinder.DeathRecipient, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun unlinkToDeath(p0: IBinder.DeathRecipient, p1: Int): Boolean {
        TODO("Not yet implemented")
    }
}