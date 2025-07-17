package com.sportscar.utils

object DefaultData {
    
    /**
     * Default JSON data for testing (simulating what would come from JS)
     */
    val defaultJsonData = """
    {
        "layoutType": "GRID",
        "rootItems": [
            {
                "id": "albums_id",
                "title": "Albums",
                "subtitle": "Browse by albums",
                "isPlayable": false,
                "children": [
                    {
                        "id": "album_thriller",
                        "title": "Thriller",
                        "subtitle": "Michael Jackson",
                        "iconUrl": "https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D",
                        "isPlayable": false,
                        "children": [
                            {
                                "id": "track_billie_jean",
                                "title": "Billie Jean",
                                "subtitle": "Michael Jackson",
                                "iconUrl": "https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D",
                                "isPlayable": true,
                                "mediaUrl": "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
                            },
                            {
                                "id": "track_beat_it",
                                "title": "Beat It",
                                "subtitle": "Michael Jackson",
                                "iconUrl": "https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D",
                                "isPlayable": true,
                                "mediaUrl": "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
                            }
                        ]
                    },
                    {
                        "id": "album_black",
                        "title": "Back in Black",
                        "subtitle": "AC/DC",
                        "iconUrl": "https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D",
                        "isPlayable": false,
                        "children": [
                            {
                                "id": "track_hells_bells",
                                "title": "Hells Bells",
                                "subtitle": "AC/DC",
                                "iconUrl": "https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D",
                                "isPlayable": true,
                                "mediaUrl": "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
                            },
                            {
                                "id": "track_shoot_thrill",
                                "title": "Shoot to Thrill",
                                "subtitle": "AC/DC",
                                "iconUrl": "https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D",
                                "isPlayable": true,
                                "mediaUrl": "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"
                            }
                        ]
                    }
                ]
            },
            {
                "id": "artists_id",
                "title": "Artists",
                "subtitle": "Browse by artists",
                "isPlayable": false,
                "children": [
                    {
                        "id": "artist_mj",
                        "title": "Michael Jackson",
                        "subtitle": "Pop Legend",
                        "iconUrl": "https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D",
                        "isPlayable": false,
                        "children": [
                            {
                                "id": "album_thriller",
                                "title": "Thriller",
                                "subtitle": "Album",
                                "iconUrl": "https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D",
                                "isPlayable": false
                            }
                        ]
                    },
                    {
                        "id": "artist_acdc",
                        "title": "AC/DC",
                        "subtitle": "Rock Band",
                        "iconUrl": "https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D",
                        "isPlayable": false,
                        "children": [
                            {
                                "id": "album_black",
                                "title": "Back in Black",
                                "subtitle": "Album",
                                "iconUrl": "https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D",
                                "isPlayable": false
                            }
                        ]
                    }
                ]
            },
            {
                "id": "playlists_id",
                "title": "Playlists",
                "subtitle": "Your playlists",
                "isPlayable": false,
                "children": [
                    {
                        "id": "playlist_workout",
                        "title": "Workout Mix",
                        "subtitle": "Mixed Artists",
                        "iconUrl": "https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D",
                        "isPlayable": true,
                        "mediaUrl": "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3"
                    }
                ]
            }
        ]
    }
    """.trimIndent()
    
    /**
     * Example JSON data with LIST layout for testing
     */
    val listLayoutJsonData = """
    {
        "layoutType": "LIST",
        "rootItems": [
            {
                "id": "albums_id",
                "title": "Albums",
                "subtitle": "Browse by albums",
                "isPlayable": false,
                "children": [
                    {
                        "id": "album_thriller",
                        "title": "Thriller",
                        "subtitle": "Michael Jackson",
                        "isPlayable": false,
                        "children": [
                            {
                                "id": "track_billie_jean",
                                "title": "Billie Jean",
                                "subtitle": "Michael Jackson",
                                "isPlayable": true,
                                "mediaUrl": "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
                            },
                            {
                                "id": "track_beat_it",
                                "title": "Beat It",
                                "subtitle": "Michael Jackson",
                                "isPlayable": true,
                                "mediaUrl": "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
                            }
                        ]
                    }
                ]
            },
            {
                "id": "artists_id",
                "title": "Artists",
                "subtitle": "Browse by artists",
                "isPlayable": false,
                "children": [
                    {
                        "id": "artist_mj",
                        "title": "Michael Jackson",
                        "subtitle": "Pop Legend",
                        "isPlayable": false
                    }
                ]
            }
        ]
    }
    """.trimIndent()
    
    /**
     * Example JSON data with mixed icons (some with, some without)
     */
    val mixedIconsJsonData = """
    {
        "layoutType": "GRID",
        "rootItems": [
            {
                "id": "albums_id",
                "title": "Albums",
                "subtitle": "Browse by albums",
                "iconUrl": "https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D",
                "isPlayable": false,
                "children": [
                    {
                        "id": "album_thriller",
                        "title": "Thriller",
                        "subtitle": "Michael Jackson",
                        "isPlayable": false,
                        "children": [
                            {
                                "id": "track_billie_jean",
                                "title": "Billie Jean",
                                "subtitle": "Michael Jackson",
                                "isPlayable": true,
                                "iconUrl": "https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D",
                                "mediaUrl": "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
                            },
                            {
                                "id": "track_beat_it",
                                "title": "Beat It",
                                "subtitle": "Michael Jackson",
                                "isPlayable": true,
                                "mediaUrl": "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
                            }
                        ]
                    },
                    {
                        "id": "album_black",
                        "title": "Back in Black",
                        "subtitle": "AC/DC",
                        "isPlayable": false,
                        "children": [
                            {
                                "id": "track_hells_bells",
                                "title": "Hells Bells",
                                "subtitle": "AC/DC",
                                "isPlayable": true,
                                "mediaUrl": "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
                            }
                        ]
                    }
                ]
            },
            {
                "id": "artists_id",
                "title": "Artists",
                "subtitle": "Browse by artists",
                "isPlayable": false,
                "children": [
                    {
                        "id": "artist_mj",
                        "title": "Michael Jackson",
                        "subtitle": "Pop Legend",
                        "iconUrl": "https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D",
                        "isPlayable": false
                    },
                    {
                        "id": "artist_acdc",
                        "title": "AC/DC",
                        "subtitle": "Rock Band",
                        "isPlayable": false
                    }
                ]
            }
        ]
    }
    """.trimIndent()
} 