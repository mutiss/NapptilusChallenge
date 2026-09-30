package com.mutissx.napptilusrickandmorty.core.common

/**
 * Carries a typed [DataError] through APIs that can only transport a [Throwable]
 * (e.g. Paging's LoadResult.Error), so the UI can still map it to a precise message.
 */
class DataException(val error: DataError) : Exception()
