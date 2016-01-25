# ListenUp API

Your task in this exercise is to implement a feature rich API for a theoretical
internet radio service called "ListenUp"


### User List

    GET /users
    Return a list of all users 
    
    {
       "users": [
            {
                "username": "joe_example",
                "plays": 178,
                "friends": 7,
                "uri": "/users/joe_example"
            },
            ... snip additional users ...
        ],
        "uri": "/users",
    }

### User Instance

    GET /users/joe_example
    Return detailed information about a user
    
    {
        "username": "joe_example",
        "plays": 178,
        "friends": 7,
        "tracks": 23,
        "uri": "/users/joe_example",
        "subresource_uris": {
            "trending": "/users/joe_example/trending",
        }
    }

### User Trending

    GET /users/joe_example/trending
    
    {
        "tracks": [
            "E75C38C1-E2BB-BAF6-620B-9255D035B693",
            "B3CA64C2-7A52-FD9A-3252-2A2FB7AD43C1",
            ... snip additional tracks ...
        ]
        "uri": "/users/joe_example/trending"
    }


"Trending" shows songs that are enjoyed by all ListenUp users but with a focus 
on songs that a user's friends enjoy

The trending list should be calculated using the following algorithm:

1.  Create the unique set of tracks listened to by the user and all their 
    friends, this is our candidate set for the trending list, call this 
    `friend tracks`.
2.  Calculate a `friend score` for each track in `friend track`.  
    This is calculated by taking the total number of plays for all friends, 
    `friend plays` multiplied by how many friends have the song in their 
    play history, `friend count`, divided by the total number of 
    friends.
3.  The `friend score` should then be multiplied by the `rating ratio`, 
    which is the `rating` divided by ten, this gives us the `trend score`
4.  Finally we clamp the data set by removing any elements that have a score 
    that is less than 20% of the top `trend score`
        
Example:

Let's use an example to make this clear.  Bill is friends with Sue, Sarah, and 
James.

    Bill's play history:  [A, A, B, C, D, J, J]
    Sue's play history:   [A, A, A, A, D]           // Sue really likes A
    Sarah's play history: [K, J, J, J, A, A]
    James' play history:  [A, B, B, C, C, J]

The `friend tracks` in this case would be [A, B, C, D, J, K]

For each `friend track` we can calculate `friend plays`, `friend count` and do 
some simple math to get the `friend score`

    track | Bill | Sue | Sarah | James | total plays | friend count |      score
    ------+------+-----+-------+-------+-------------+--------------+-----------------
      A   |  2   |  4  |  2    |  1    |      9      |      4       |  9 * (4/4) = 9
      B   |  1   |  0  |  0    |  2    |      3      |      2       |  3 * (2/4) = 1.5 
      C   |  1   |  0  |  0    |  2    |      3      |      2       |  3 * (2/4) = 1.5
      D   |  1   |  1  |  0    |  0    |      2      |      2       |  2 * (2/4) = 1
      J   |  2   |  0  |  3    |  1    |      6      |      3       |  6 * (3/4) = 4.5
      K   |  0   |  0  |  1    |  0    |      1      |      1       |  1 * (1/4) = 0.25
      
Next we multiply in the global ratings

    track | friend score | rating | trend score
    ------+--------------+--------+-------------
      A   |     9        |  4     |   9 * (4/10)  = 3.6
      B   |     1.5      |  10    | 1.5 * (10/10) = 1.5
      C   |     1.5      |  4     | 1.5 * (4/10)  = 0.6
      D   |     1        |  9     |   1 * (9/10)  = 0.9
      J   |     4.5      |  10    | 4.5 * (10/10) = 4.5
      K   |     0.25     |  3     | 0.25 * (3/10) = 0.075

This give us overall trend list of 

    [
        J (4.5),
        A (3.6),
        B (1.5),
        D (0.9),
        C (0.6),
        K (0.075)
    ]

Next we clamp this set by eliminating any track that has less than 20% of the 
top score (4.5 * 0.2 = 0.9).  This would eliminate C (0.6) and K (0.075)

The end result would be `[J, A, B, D]`

