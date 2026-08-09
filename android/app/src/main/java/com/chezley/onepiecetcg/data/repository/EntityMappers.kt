package com.chezley.onepiecetcg.data.repository

import com.chezley.onepiecetcg.data.db.CardEntity
import com.chezley.onepiecetcg.data.db.CardSetEntity
import com.chezley.onepiecetcg.data.db.OwnedCardEntity
import com.chezley.onepiecetcg.data.model.Card
import com.chezley.onepiecetcg.data.model.CardSet
import com.chezley.onepiecetcg.data.model.OwnedCard

internal fun CardEntity.toDomain(): Card =
    Card(
        id = id,
        name = name,
        setCode = setCode,
        cardNumber = cardNumber,
        rarity = rarity,
        cost = cost,
        power = power,
        attribute = attribute,
        type = type,
        imageUrl = imageUrl,
    )

internal fun CardSetEntity.toDomain(): CardSet = CardSet(code = code, name = name, releaseDate = releaseDate)

internal fun OwnedCardEntity.toDomain(card: Card): OwnedCard =
    OwnedCard(id = id, card = card, quantity = quantity, condition = condition, dateAdded = dateAdded)
