package hu.nagyi.sixthweekwork

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import hu.nagyi.sixthweekwork.adapter.AdvertisementsAdapter
import hu.nagyi.sixthweekwork.data.Advertisement
import hu.nagyi.sixthweekwork.databinding.ActivityAdvertisementBinding
import java.util.*

import com.google.firebase.firestore.QuerySnapshot

import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception

class AdvertisementActivity : AppCompatActivity() {

    //region VARIABLES

    lateinit var binding: ActivityAdvertisementBinding

    private lateinit var advertisementsAdapter: AdvertisementsAdapter

    private lateinit var eventListener: EventListener<QuerySnapshot>
    private lateinit var queryRef: CollectionReference
    private var listenerReg: ListenerRegistration? = null

    //endregion

    //region METHODS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.binding = ActivityAdvertisementBinding.inflate(this.layoutInflater)
        this.setContentView(this.binding.root)

        this.setSupportActionBar(this.binding.toolbar)
        this.binding.toolbarLayout.title = title

        this.binding.createAdvertisementFAB.setOnClickListener {
            this.startActivity(Intent(this, CreateAdvertisementActivity::class.java))
        }

        this.initAdapterAndSetItToRV()

        this.initFirebaseQuery()
    }

    private fun initAdapterAndSetItToRV() {
        this.advertisementsAdapter =
            AdvertisementsAdapter(this, FirebaseAuth.getInstance().currentUser!!.uid)
        this.binding.advertisementsRV.adapter = this.advertisementsAdapter
    }

    private fun addAdToAdapter(documents: QuerySnapshot) {
        for (document in documents) {
            val ad = document.toObject(Advertisement::class.java)
            this.advertisementsAdapter.addAdvertisement(ad, document.id)
        }
    }

    private fun showToastError(exception: Exception) {
        Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
    }

    private fun initFirebaseQuery() {
        this.queryRef = FirebaseFirestore.getInstance()
            .collection(Constants.COLLECTION_ADVERTISEMENTS)

        this.binding.filterSV.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    if (newText.isEmpty()) {
                        this@AdvertisementActivity.queryRef.get()
                            .addOnSuccessListener { documents ->
                                this@AdvertisementActivity.initAdapterAndSetItToRV()
                                this@AdvertisementActivity.addAdToAdapter(documents)
                            }
                            .addOnFailureListener { exception ->
                                this@AdvertisementActivity.showToastError(exception)
                            }
                    } else {
                        this@AdvertisementActivity.queryRef.whereEqualTo("title", newText).get()
                            .addOnSuccessListener { documents ->
                                this@AdvertisementActivity.initAdapterAndSetItToRV()
                                this@AdvertisementActivity.addAdToAdapter(documents)
                            }
                            .addOnFailureListener { exception ->
                                this@AdvertisementActivity.showToastError(exception)
                            }
                    }
                    return true
                }
            })


        this.eventListener = object : EventListener<QuerySnapshot> {
            override fun onEvent(querySnapshot: QuerySnapshot?, e: FirebaseFirestoreException?) {
                if (e != null) {
                    Toast.makeText(
                        this@AdvertisementActivity, "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                for (docChange in querySnapshot?.documentChanges!!) {
                    when (docChange.type) {
                        DocumentChange.Type.ADDED -> {
                            val advertisement =
                                docChange.document.toObject(Advertisement::class.java)
                            this@AdvertisementActivity.advertisementsAdapter.addAdvertisement(
                                advertisement, docChange.document.id
                            )
                        }
                        DocumentChange.Type.REMOVED -> {
                            this@AdvertisementActivity.advertisementsAdapter.removeAdvertisementByKey(
                                docChange.document.id
                            )
                        }
                        DocumentChange.Type.MODIFIED -> {

                        }
                    }
                }

            }
        }

        this.listenerReg = this.queryRef.addSnapshotListener(this.eventListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        this.listenerReg?.remove()
    }

    //endregion

}
