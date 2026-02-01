package com.kankarej.kankarejspices.model

data class ContactInfo(
    val gstin: String = "",
    val fssai: String = "",
    val address: String = "",
    // We will map the 'team' node manually in the repo, 
    // so we don't strictly need it in the constructor if we use a helper class, 
    // but a separate list makes UI easier.
    val teamList: List<ContactPerson> = emptyList()
)

data class ContactPerson(
    val name: String = "",
    val role: String = "",
    val phone: String = ""
)