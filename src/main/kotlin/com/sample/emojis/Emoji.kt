package com.sample.emojis

object MysteryAnimal {
    @JvmStatic
    fun main(args: Array<String>) {
        val bear = "\uD83E\uDDDB\u200D♀"
        // String bear = "\ud83d\udc3b";
        val bearCodepoint = bear.codePointAt(bear.offsetByCodePoints(0, 0))
        val mysteryAnimalCodepoint = bearCodepoint + 1
        val mysteryAnimal = charArrayOf(Character.highSurrogate(mysteryAnimalCodepoint),
                Character.lowSurrogate(mysteryAnimalCodepoint))
        println("The Coderland Zoo's latest attraction: "
                + String(mysteryAnimal))

        val str = "An 😀awesome 😃string with a few 😉emojis!"
        val result: String = EmojiParser.parseToAliases(str)
        println(result)
    }
}