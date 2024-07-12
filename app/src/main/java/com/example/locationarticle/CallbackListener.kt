package com.example.locationarticle

/**
 * An interface defining a callback listener with a generic type.
 *
 * This interface defines a contract for classes that listen for callbacks with a generic data type.
 * The `onCallback` method is called by the sender to deliver data or notifications to the listener.
 *
 * @param <T> The type of data associated with the callback.
 */
interface CallbackListener<T> {

    /**
     * Called by the sender to deliver data to the listener.
     *
     * Implement this method in the listener to handle the provided callback object
     * of type `T`
     *
     * @param callback The callback object.
     */
    fun onCallback(callback: T)
}