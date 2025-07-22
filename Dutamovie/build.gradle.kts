version = 2

cloudstream {
    description = "DutaMovie"
    language = "id"
    authors = listOf("CodeSannz")

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
     * */
    status = 1 // will be 3 if unspecified
    tvTypes = listOf(
        "AsianDrama",
        "TvSeries",
        "Movie",
    )

    iconUrl = "https://github.com/CodeSannz/IndoMovie/raw/refs/heads/master/Dutamovie/icon.png"
}

