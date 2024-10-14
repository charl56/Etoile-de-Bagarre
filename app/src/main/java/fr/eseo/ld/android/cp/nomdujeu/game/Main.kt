package fr.eseo.ld.android.cp.nomdujeu.game

import com.badlogic.gdx.Application
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.ScreenUtils


class Main : ApplicationAdapter(), ApplicationListener {
    private lateinit var backgroundTexture: Texture
    private lateinit var bucketTexture: Texture
    private lateinit var dropTexture: Texture
    private lateinit var dropSound: Sound
    private lateinit var music: Music
    private lateinit var spriteBatch: SpriteBatch
    private lateinit var viewport: FitViewport
    private lateinit var bucketSprite: Sprite
    private lateinit var touchPos: Vector2
    private lateinit var dropSprites: Array<Sprite>
    private var dropTimer = 0f
    private lateinit var bucketRectangle: Rectangle
    private lateinit var dropRectangle: Rectangle

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG

        backgroundTexture = Texture("background.png")
        bucketTexture = Texture("bucket.png")
        dropTexture = Texture("drop.png")
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"))
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"))
        spriteBatch = SpriteBatch()
        viewport = FitViewport(8f, 5f)
        bucketSprite = Sprite(bucketTexture).apply { setSize(1f, 1f) }
        touchPos = Vector2()
        dropSprites = Array()
        bucketRectangle = Rectangle()
        dropRectangle = Rectangle()
        music.isLooping = true
        music.volume = 0.5f
        music.play()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun render() {
        input()
        logic()
        draw()
    }

    private fun input() {
        val speed = 4f
        val delta = Gdx.graphics.deltaTime

        if (Gdx.input.isTouched) {
            touchPos.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            viewport.unproject(touchPos)
            bucketSprite.setCenterX(touchPos.x)
        }
    }

    private fun logic() {
        val worldWidth = viewport.worldWidth
        val worldHeight = viewport.worldHeight
        val bucketWidth = bucketSprite.width
        val bucketHeight = bucketSprite.height

        bucketSprite.x = MathUtils.clamp(bucketSprite.x, 0f, worldWidth - bucketWidth)

        val delta = Gdx.graphics.deltaTime
        bucketRectangle.set(bucketSprite.x, bucketSprite.y, bucketWidth, bucketHeight)

        for (i in dropSprites.size - 1 downTo 0) {
            val dropSprite = dropSprites[i]
            val dropWidth = dropSprite.width
            val dropHeight = dropSprite.height

            dropSprite.translateY(-2f * delta)
            dropRectangle.set(dropSprite.x, dropSprite.y, dropWidth, dropHeight)

            when {
                dropSprite.y < -dropHeight -> dropSprites.removeIndex(i)
                bucketRectangle.overlaps(dropRectangle) -> {
                    dropSprites.removeIndex(i)
                    dropSound.play()
                }
            }
        }

        dropTimer += delta
        if (dropTimer > 1f) {
            dropTimer = 0f
            createDroplet()
        }
    }

    private fun draw() {
        ScreenUtils.clear(Color.BLACK)
        viewport.apply()
        spriteBatch.projectionMatrix = viewport.camera.combined
        spriteBatch.begin()

        val worldWidth = viewport.worldWidth
        val worldHeight = viewport.worldHeight

        spriteBatch.draw(backgroundTexture, 0f, 0f, worldWidth, worldHeight)
        bucketSprite.draw(spriteBatch)

        for (dropSprite in dropSprites) {
            dropSprite.draw(spriteBatch)
        }

        spriteBatch.end()
    }

    private fun createDroplet() {
        val dropWidth = 1f
        val dropHeight = 1f
        val worldWidth = viewport.worldWidth
        val worldHeight = viewport.worldHeight

        val dropSprite = Sprite(dropTexture).apply {
            setSize(dropWidth, dropHeight)
            x = MathUtils.random(0f, worldWidth - dropWidth)
            y = worldHeight
        }
        dropSprites.add(dropSprite)
    }

    override fun pause() {}

    override fun resume() {}

    override fun dispose() {}
}