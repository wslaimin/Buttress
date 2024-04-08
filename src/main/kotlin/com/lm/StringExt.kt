package com.lm

fun String.capitalize():String{
    return if(this.isNotEmpty()){
        this.replaceFirstChar {
            it.uppercaseChar()
        }
    }else{
        this
    }
}

fun String.uncapitalize():String{
    return if(this.isNotEmpty()){
        this.replaceFirstChar {
            it.lowercase()
        }
    }else{
        this
    }
}