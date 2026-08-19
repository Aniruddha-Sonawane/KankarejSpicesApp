package com.kankarej.kankarejspices.model

data class ContactInfo(
    val gstin: String = "",
    val fssai: String = "",
    val address: String = "",
    val teamList: List<ContactPerson> = emptyList(),

    // Firebase: contact_info/whatsapp
    val whatsappNumber: String = "",

    // Firebase: contact_info/phone
    val phoneNumber: String = ""
)

data class ContactPerson(
    val name: String = "",
    val role: String = "",
    val phone: String = ""
)
