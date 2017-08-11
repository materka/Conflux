package se.materka.conflux.service

/**
 * Created by Mattias on 2/19/2017.
 */
enum class RadioEvent {
    STATUS_LOADING,
    STATUS_PLAYING,
    STATUS_STOPPED,
    ERROR_INVALID_URL,
    ERROR_NO_VALID_URL_FOUND
}