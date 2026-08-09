package com.chezley.onepiecetcg.data.repository

/** Thrown when an owned-card operation is passed a zero/negative quantity. */
class InvalidQuantityException(message: String) : IllegalArgumentException(message)
