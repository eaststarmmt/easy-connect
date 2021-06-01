package kr.ac.cau.easyconnect

// Firebase-firestore에 저장될 user 데이터 클래스 정의

data class UserDTO(var email:String? = null, var password:String? = null, var name:String? = null, var phoneNumber:String? = null, var photo:String? = null, var uid:String? = null, var search:Boolean? = false, var following:String? = null, var followed:String? = null, var gender:String? = null, var age:String? = null)
