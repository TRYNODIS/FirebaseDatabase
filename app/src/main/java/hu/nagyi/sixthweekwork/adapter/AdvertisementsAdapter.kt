package hu.nagyi.sixthweekwork.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import hu.nagyi.sixthweekwork.Constants
import hu.nagyi.sixthweekwork.data.Advertisement
import hu.nagyi.sixthweekwork.databinding.RowAdvertisementBinding

class AdvertisementsAdapter(var context: Context, var currentUID: String) :
    RecyclerView.Adapter<AdvertisementsAdapter.ViewHolder>() {

    //region VARIABLES

    var advertisementsList = mutableListOf<Advertisement>()
    var advertisementKeys = mutableListOf<String>()

    //endregion

    //region METHODS

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowAdvertisementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return this.advertisementsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val advertisement = this.advertisementsList[holder.adapterPosition]

        holder.titleTV.text = advertisement.title
        holder.descriptionTV.text = advertisement.description
        holder.priceTV.text = advertisement.price
        holder.emailTV.text = advertisement.email
        holder.phoneTV.text = advertisement.phone

        if (this.currentUID == advertisement.uid) {
            holder.deleteBtn.visibility = View.VISIBLE
        } else {
            holder.deleteBtn.visibility = View.GONE
        }

        holder.modifyBtn.setOnClickListener {
            Toast.makeText(this.context, "This feature is in development", Toast.LENGTH_LONG).show()
            //((context as AppCompatActivity)).startActivity(Intent(this.context, ModifyAdvertisementActivity::class.java))
        }

        holder.deleteBtn.setOnClickListener {
            this.removeAdvertisement(holder.adapterPosition)
        }

        if (advertisement.imgUrl.isNotEmpty()) {
            holder.photoIV.visibility = View.VISIBLE
            Glide.with(this.context).load(advertisement.imgUrl).into(holder.photoIV)
        } else {
            holder.photoIV.visibility = View.GONE
        }
    }

    fun addAdvertisement(advertisement: Advertisement, key: String) {
        this.advertisementsList.add(advertisement)
        this.advertisementKeys.add(key)
        this.notifyItemInserted(this.advertisementsList.lastIndex)
    }

    private fun removeAdvertisement(index: Int) {
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ADVERTISEMENTS).document(
            this.advertisementKeys[index]
        ).delete()

        this.advertisementsList.removeAt(index)
        this.advertisementKeys.removeAt(index)
        this.notifyItemRemoved(index)
    }

    fun removeAdvertisementByKey(key: String) {
        val index = this.advertisementKeys.indexOf(key)
        if (index != -1) {
            this.advertisementsList.removeAt(index)
            this.advertisementKeys.removeAt(index)
            this.notifyItemRemoved(index)
        }
    }

    //endregion

    //region INNER CLASS VIEW HOLDER

    inner class ViewHolder(private val binding: RowAdvertisementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var titleTV = this.binding.titleTV
        var descriptionTV = this.binding.descriptionTV
        var priceTV = this.binding.priceTV
        var emailTV = this.binding.emailTV
        var phoneTV = this.binding.phoneTV
        var photoIV = this.binding.photoIV
        var modifyBtn = this.binding.modifyBtn
        var deleteBtn = this.binding.deleteBtn
    }

    //endregion
}