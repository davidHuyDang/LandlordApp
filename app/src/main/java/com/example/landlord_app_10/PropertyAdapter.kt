package com.example.landlord_app_10

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PropertyAdapter(
    private val properties: MutableList<Property>,
    private val onEdit: (Property) -> Unit,
    private val onDelete: (Property) -> Unit,
    private val onToggle: (Property, Boolean) -> Unit
) : RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder>() {

    // ViewHolder for each property item.
    class PropertyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val propertyImage: ImageView =
            view.findViewById(R.id.imageProperty) // ImageView for property image.
        val address: TextView =
            view.findViewById(R.id.tvAddress) // TextView for the property address.
        val price: TextView = view.findViewById(R.id.tvPrice) // TextView for the property price.
        val bedrooms: TextView =
            view.findViewById(R.id.tvBedrooms) // TextView for the number of bedrooms.
        val availabilitySwitch: Switch =
            view.findViewById(R.id.switchItemAvailable) // Switch to toggle availability.
        val editButton: View = view.findViewById(R.id.btnEdit) // Button to edit property.
        val deleteButton: View = view.findViewById(R.id.btnDelete) // Button to delete property.
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.property_item, parent, false)
        return PropertyViewHolder(view)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = properties[position]

        // Set text for address, price, and number of bedrooms.
        holder.address.text = "${property.address}"
        holder.price.text = "Price: $${String.format("%.2f", property.price)}"
        holder.bedrooms.text = "Bedrooms: ${property.bedrooms}"

        // Set the initial state of the availability switch based on the property data.
        holder.availabilitySwitch.isChecked = property.available

        // Load the property image using Glide library.
        Glide.with(holder.itemView.context)
            .load(property.imageUrl)
            .centerCrop() // Crop the image to fit the ImageView while maintaining the aspect ratio.
            .placeholder(R.drawable.placeholder_image) // Display placeholder while image is loading.
            .error(R.drawable.placeholder_image) // Display error image if loading fails.
            .into(holder.propertyImage)

        // Set the listener for the availability switch, invoking onToggle when the state changes.
        holder.availabilitySwitch.setOnCheckedChangeListener { _, isChecked ->
            onToggle(property, isChecked)
        }

        // Set the listener for the edit button, invoking onEdit when clicked.
        holder.editButton.setOnClickListener { onEdit(property) }

        // Set the listener for the delete button, invoking onDelete when clicked.
        holder.deleteButton.setOnClickListener { onDelete(property) }
    }

    // Return the total number of items in the properties list.
    override fun getItemCount(): Int = properties.size

    // Function to update the list of properties and refresh the RecyclerView.
    fun updateProperties(newProperties: List<Property>) {
        properties.clear() // Clear current list.
        properties.addAll(newProperties) // Add the new list of properties.
        notifyDataSetChanged() // Notify the adapter that the data has changed.
    }
}