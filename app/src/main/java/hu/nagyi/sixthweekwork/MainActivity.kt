package hu.nagyi.sixthweekwork

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import hu.nagyi.sixthweekwork.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    //region VARIABLES

    lateinit var binding: ActivityMainBinding

    //endregion

    //region METHODS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityMainBinding.inflate(this.layoutInflater)
        this.setContentView(this.binding.root)

        //FirebaseMessaging.getInstance().subscribeToTopic("forumpushes")
    }

    fun loginClick(v: View) {
        if (!isFormValid()) {
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(
            this.binding.emailET.text.toString(),
            this.binding.pwET.text.toString()
        ).addOnSuccessListener {
            this.startActivity(Intent(this@MainActivity, AdvertisementActivity::class.java))
        }.addOnFailureListener {
            Toast.makeText(
                this@MainActivity,
                "Login error: ${it.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun registerClick(v: View) {
        if (!isFormValid()) {
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
            this.binding.emailET.text.toString(),
            this.binding.pwET.text.toString()
        ).addOnSuccessListener {
            Toast.makeText(
                this@MainActivity,
                "Registration OK",
                Toast.LENGTH_LONG
            ).show()
        }.addOnFailureListener {
            Toast.makeText(
                this@MainActivity,
                "Error: ${it.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    private fun isFormValid(): Boolean {
        return when {
            this.binding.emailET.text.isEmpty() -> {
                this.binding.emailET.error = "This field can not be empty"
                false
            }
            this.binding.pwET.text.isEmpty() -> {
                this.binding.pwET.error = "The password can not be empty"
                false
            }
            else -> true
        }
    }

    //endregion
}