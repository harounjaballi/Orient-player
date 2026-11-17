package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.core.security.DeviceIdentity

@Database(
    entities = [
        DeviceEntity::class,
        PlaylistConfigEntity::class,
        LiveChannelEntity::class,
        VodMovieEntity::class,
        SeriesEntity::class,
        EpisodeEntity::class,
        CatchUpProgramEntity::class,
        ActivationLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OrientDatabase : RoomDatabase() {
    abstract fun orientDao(): OrientDao

    companion object {
        @Volatile
        private var INSTANCE: OrientDatabase? = null

        fun getInstance(context: Context): OrientDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OrientDatabase::class.java,
                    "orient_player_iptv.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun ensureSeeded(dao: OrientDao, identity: DeviceIdentity) {
            val now = System.currentTimeMillis()
            val dayMs = 86_400_000L

            // Ensure current device is registered in DB
            val existingDevice = dao.getDeviceByMac(identity.virtualMac)
            if (existingDevice == null) {
                val currentDevice = DeviceEntity(
                    virtualMac = identity.virtualMac,
                    installationId = identity.installationId,
                    activationCode = identity.activationCode,
                    deviceKey = identity.deviceKey,
                    clientName = "Cet Appareil (${identity.deviceModel})",
                    deviceModel = identity.deviceModel,
                    deviceType = identity.deviceType,
                    status = DeviceStatus.ACTIVE.name,
                    subscriptionPlan = SubscriptionPlan.MONTH_12.name,
                    startDateMillis = now - (14 * dayMs),
                    expirationDateMillis = now + (351 * dayMs),
                    lastConnectionMillis = now,
                    securityToken = identity.securityToken
                )
                dao.upsertDevice(currentDevice)

                // Seed companion devices for the Web Admin Dashboard
                val sampleDevices = listOf(
                    DeviceEntity(
                        virtualMac = "00:1A:79:A4:8B:21",
                        installationId = "F81D4FAE-7DEC-11D0-A765-00A0C91E6BF6",
                        activationCode = "492-810",
                        deviceKey = "774129",
                        clientName = "Salon Nvidia Shield Pro",
                        deviceModel = "NVIDIA SHIELD Android TV",
                        deviceType = "Android TV / Box",
                        status = DeviceStatus.ACTIVE.name,
                        subscriptionPlan = SubscriptionPlan.PERMANENT.name,
                        startDateMillis = now - (120 * dayMs),
                        expirationDateMillis = now + (36500 * dayMs),
                        lastConnectionMillis = now - 1_800_000L,
                        securityToken = "tok_001a79a48b21_perm"
                    ),
                    DeviceEntity(
                        virtualMac = "00:1A:79:C9:12:5E",
                        installationId = "A92E1C04-5B21-48E9-9D12-883B10E92A11",
                        activationCode = "618-305",
                        deviceKey = "391044",
                        clientName = "Chambre Xiaomi Mi Box S",
                        deviceModel = "Xiaomi MiBox4",
                        deviceType = "Android TV / Box",
                        status = DeviceStatus.ACTIVE.name,
                        subscriptionPlan = SubscriptionPlan.MONTH_6.name,
                        startDateMillis = now - (45 * dayMs),
                        expirationDateMillis = now + (135 * dayMs),
                        lastConnectionMillis = now - 7_200_000L,
                        securityToken = "tok_001a79c9125e_6m"
                    ),
                    DeviceEntity(
                        virtualMac = "00:1A:79:3F:70:D4",
                        installationId = "C14B8820-90A1-44E2-B102-491827364500",
                        activationCode = "205-941",
                        deviceKey = "512908",
                        clientName = "Client Karim - Formuler Z11",
                        deviceModel = "Formuler Z11 Pro Max",
                        deviceType = "Android TV / Box",
                        status = DeviceStatus.EXPIRED.name,
                        subscriptionPlan = SubscriptionPlan.MONTH_1.name,
                        startDateMillis = now - (40 * dayMs),
                        expirationDateMillis = now - (10 * dayMs),
                        lastConnectionMillis = now - (9 * dayMs),
                        securityToken = "tok_001a793f70d4_exp"
                    ),
                    DeviceEntity(
                        virtualMac = "00:1A:79:E1:09:B8",
                        installationId = "D77E9912-11C4-4A08-8F00-112233445566",
                        activationCode = "913-447",
                        deviceKey = "108422",
                        clientName = "Appareil Suspect #44",
                        deviceModel = "Generic TV Box RK3566",
                        deviceType = "Android Box / Tablette",
                        status = DeviceStatus.BLOCKED.name,
                        subscriptionPlan = SubscriptionPlan.MONTH_3.name,
                        startDateMillis = now - (60 * dayMs),
                        expirationDateMillis = now + (30 * dayMs),
                        lastConnectionMillis = now - (3 * dayMs),
                        securityToken = "tok_001a79e109b8_blk"
                    )
                )
                dao.upsertDevices(sampleDevices)

                // Seed Default Playlists for current MAC & sample MACs
                val p1 = dao.insertPlaylist(
                    PlaylistConfigEntity(
                        deviceMac = identity.virtualMac,
                        name = "Orient Premium UHD (Xtream API)",
                        protocol = PlaylistProtocol.XTREAM.name,
                        serverUrl = "http://line.orient-iptv.pro:8080",
                        username = "orient_vip_${identity.deviceKey}",
                        password = "xp_${identity.activationCode.replace("-", "")}",
                        autoUpdateEnabled = true,
                        isActive = true,
                        channelCount = 14,
                        vodCount = 10,
                        seriesCount = 6,
                        lastUpdatedMillis = now - 600_000L
                    )
                )
                dao.insertPlaylist(
                    PlaylistConfigEntity(
                        deviceMac = identity.virtualMac,
                        name = "Bouquet International M3U8",
                        protocol = PlaylistProtocol.M3U.name,
                        serverUrl = "https://iptv-org.github.io/iptv/index.m3u",
                        autoUpdateEnabled = true,
                        isActive = true,
                        channelCount = 120,
                        vodCount = 45,
                        seriesCount = 12,
                        lastUpdatedMillis = now - 3_600_000L
                    )
                )
                dao.insertPlaylist(
                    PlaylistConfigEntity(
                        deviceMac = "00:1A:79:A4:8B:21",
                        name = "Portail MAG Ministra (Stalker)",
                        protocol = PlaylistProtocol.STALKER.name,
                        serverUrl = "http://mag.orient-portal.tv/stalker_portal/c/",
                        stalkerMac = "00:1A:79:A4:8B:21",
                        autoUpdateEnabled = true,
                        isActive = true,
                        channelCount = 340,
                        vodCount = 820,
                        seriesCount = 150,
                        lastUpdatedMillis = now - 7_200_000L
                    )
                )

                // Seed Activation & Security Logs
                dao.insertActivationLog(
                    ActivationLogEntity(
                        deviceMac = identity.virtualMac,
                        actionType = "CONNEXION",
                        description = "Vérification Token HMAC & Synchronisation Backend réussie (${identity.deviceModel})",
                        performedBy = "Système Sécurité",
                        timestampMillis = now
                    )
                )
                dao.insertActivationLog(
                    ActivationLogEntity(
                        deviceMac = identity.virtualMac,
                        actionType = "ACTIVATION",
                        description = "Activation Abonnement 12 Mois associée à la playlist Orient Premium UHD",
                        performedBy = "Admin Principal",
                        timestampMillis = now - (14 * dayMs)
                    )
                )
                dao.insertActivationLog(
                    ActivationLogEntity(
                        deviceMac = "00:1A:79:A4:8B:21",
                        actionType = "ACTIVATION",
                        description = "Activation Permanente accordée (Stalker Portal MAG)",
                        performedBy = "Admin Principal",
                        timestampMillis = now - (5 * dayMs)
                    )
                )
                dao.insertActivationLog(
                    ActivationLogEntity(
                        deviceMac = "00:1A:79:E1:09:B8",
                        actionType = "BLOCK",
                        description = "Appareil bloqué automatiquement : tentatives de connexions simultanées multiples",
                        performedBy = "Anti-Fraude Guard",
                        timestampMillis = now - (3 * dayMs)
                    )
                )

                seedChannelsMoviesAndSeries(dao, p1, now)
            } else if (dao.getChannelCount() == 0) {
                seedChannelsMoviesAndSeries(dao, 1L, now)
            }
        }

        private suspend fun seedChannelsMoviesAndSeries(dao: OrientDao, playlistId: Long, now: Long) {
            // Reliable, open-source HLS (.m3u8) and MP4 streams that play smoothly in ExoPlayer
            val hlsTearsOfSteel = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
            val hlsBipBop = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8"
            val hlsSintel = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"
            val mp4BigBuckBunny = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            val mp4ElephantsDream = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
            val mp4Sintel = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
            val mp4TearsOfSteel = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
            val mp4ForBiggerBlazes = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
            val mp4ForBiggerEscapes = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
            val mp4ForBiggerFun = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
            val mp4ForBiggerJoy = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4"
            val mp4WeAreGoingOnBullrun = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4"
            val mp4WhatCarCanYouGet = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4"

            val channels = listOf(
                LiveChannelEntity(
                    playlistId = playlistId,
                    channelNumber = 1,
                    name = "ORIENT SPORT 1 UHD",
                    category = "Sport HD",
                    logoUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=300&q=80",
                    streamUrl = hlsTearsOfSteel,
                    streamFormat = "HLS",
                    epgChannelId = "orient.sport1.uhd",
                    currentProgramTitle = "Ligue des Champions : Soirée Multiplex Direct",
                    currentProgramDesc = "Suivez en direct intégral les grandes affiches européennes en Ultra Haute Définition avec analyses tactiques.",
                    currentProgramStart = "20:45",
                    currentProgramEnd = "23:00",
                    currentProgramProgress = 0.62f,
                    nextProgramTitle = "Le Grand Débrief Sport",
                    nextProgramTime = "23:00 - 00:15",
                    hasCatchUp = true,
                    catchUpDays = 7,
                    isFavorite = true,
                    lastWatchedMillis = now - 120_000L
                ),
                LiveChannelEntity(
                    playlistId = playlistId,
                    channelNumber = 2,
                    name = "ORIENT CINÉMA PREMIER",
                    category = "Cinéma & Séries",
                    logoUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=300&q=80",
                    streamUrl = hlsSintel,
                    streamFormat = "HLS",
                    epgChannelId = "orient.cinema.premier",
                    currentProgramTitle = "Sintel : La Légende du Dragon (4K HDR)",
                    currentProgramDesc = "Une jeune guerrière parcourt des terres glacées à la recherche d'un jeune dragon nommé Scales.",
                    currentProgramStart = "21:00",
                    currentProgramEnd = "22:45",
                    currentProgramProgress = 0.45f,
                    nextProgramTitle = "Les Coulisses du 7e Art",
                    nextProgramTime = "22:45 - 23:30",
                    hasCatchUp = true,
                    catchUpDays = 7,
                    isFavorite = true,
                    lastWatchedMillis = now - 600_000L
                ),
                LiveChannelEntity(
                    playlistId = playlistId,
                    channelNumber = 3,
                    name = "ORIENT INFO 24/7",
                    category = "Actualités",
                    logoUrl = "https://images.unsplash.com/photo-1495020689067-958852a7765e?w=300&q=80",
                    streamUrl = hlsBipBop,
                    streamFormat = "HLS",
                    epgChannelId = "orient.info24",
                    currentProgramTitle = "Le Journal International du Soir",
                    currentProgramDesc = "Toute l'actualité géopolitique, économique et technologique en direct avec nos correspondants.",
                    currentProgramStart = "21:00",
                    currentProgramEnd = "22:00",
                    currentProgramProgress = 0.78f,
                    nextProgramTitle = "Économie & Marchés Mondiaux",
                    nextProgramTime = "22:00 - 22:30",
                    hasCatchUp = true,
                    catchUpDays = 3,
                    isFavorite = false
                ),
                LiveChannelEntity(
                    playlistId = playlistId,
                    channelNumber = 4,
                    name = "ATLAS DISCOVERY 4K",
                    category = "Documentaires",
                    logoUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=300&q=80",
                    streamUrl = mp4ForBiggerEscapes,
                    streamFormat = "MP4",
                    epgChannelId = "atlas.discovery.4k",
                    currentProgramTitle = "Odyssée des Océans et Sommets",
                    currentProgramDesc = "Exploration spectaculaire des sanctuaires naturels les plus reculés de la planète.",
                    currentProgramStart = "20:30",
                    currentProgramEnd = "22:10",
                    currentProgramProgress = 0.55f,
                    nextProgramTitle = "Mystères du Cosmos",
                    nextProgramTime = "22:10 - 23:40",
                    hasCatchUp = true,
                    catchUpDays = 7,
                    isFavorite = true
                ),
                LiveChannelEntity(
                    playlistId = playlistId,
                    channelNumber = 5,
                    name = "ORIENT SPORT 2 ARENA",
                    category = "Sport HD",
                    logoUrl = "https://images.unsplash.com/photo-1541534741688-6078c6bfb5c5?w=300&q=80",
                    streamUrl = mp4WeAreGoingOnBullrun,
                    streamFormat = "MP4",
                    epgChannelId = "orient.sport2.arena",
                    currentProgramTitle = "Grand Prix GT : Circuit International",
                    currentProgramDesc = "Course d'endurance et essais chronométrés en caméra embarquée.",
                    currentProgramStart = "21:15",
                    currentProgramEnd = "23:15",
                    currentProgramProgress = 0.31f,
                    nextProgramTitle = "Paddock Magazine",
                    nextProgramTime = "23:15 - 00:00",
                    hasCatchUp = true,
                    catchUpDays = 5
                ),
                LiveChannelEntity(
                    playlistId = playlistId,
                    channelNumber = 6,
                    name = "ORIENT KIDS & TOONS",
                    category = "Jeunesse",
                    logoUrl = "https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?w=300&q=80",
                    streamUrl = mp4BigBuckBunny,
                    streamFormat = "MP4",
                    epgChannelId = "orient.kids.hd",
                    currentProgramTitle = "Les Aventures de Big Buck Bunny",
                    currentProgramDesc = "Dessin animé familial : l'histoire drôle et attachante d'un lapin géant au grand cœur.",
                    currentProgramStart = "20:50",
                    currentProgramEnd = "21:45",
                    currentProgramProgress = 0.70f,
                    nextProgramTitle = "Contes Étoilés",
                    nextProgramTime = "21:45 - 22:30",
                    hasCatchUp = true,
                    catchUpDays = 7
                ),
                LiveChannelEntity(
                    playlistId = playlistId,
                    channelNumber = 7,
                    name = "ORIENT SCI-FI & ACTION",
                    category = "Cinéma & Séries",
                    logoUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=300&q=80",
                    streamUrl = mp4TearsOfSteel,
                    streamFormat = "MP4",
                    epgChannelId = "orient.scifi.hd",
                    currentProgramTitle = "Tears of Steel : Cyber Amsterdam",
                    currentProgramDesc = "Dans un futur proche à Amsterdam, une équipe de scientifiques et de guerriers tente de sauver le monde.",
                    currentProgramStart = "21:00",
                    currentProgramEnd = "22:50",
                    currentProgramProgress = 0.40f,
                    nextProgramTitle = "Matrix Chronicles",
                    nextProgramTime = "22:50 - 00:30",
                    hasCatchUp = true,
                    catchUpDays = 7
                ),
                LiveChannelEntity(
                    playlistId = playlistId,
                    channelNumber = 8,
                    name = "AL-SHARQ CULTURE HD",
                    category = "Monde & Orient",
                    logoUrl = "https://images.unsplash.com/photo-1512632578888-169bbbc64f33?w=300&q=80",
                    streamUrl = mp4ForBiggerBlazes,
                    streamFormat = "MP4",
                    epgChannelId = "alsharq.culture.hd",
                    currentProgramTitle = "Soirée Symphonique & Patrimoine d'Orient",
                    currentProgramDesc = "Voyage architectural et musical à travers les grandes capitales méditerranéennes et orientales.",
                    currentProgramStart = "21:00",
                    currentProgramEnd = "22:30",
                    currentProgramProgress = 0.50f,
                    nextProgramTitle = "Gastronomie des Palais",
                    nextProgramTime = "22:30 - 23:30",
                    hasCatchUp = true,
                    catchUpDays = 7,
                    isFavorite = true
                ),
                LiveChannelEntity(
                    playlistId = playlistId,
                    channelNumber = 9,
                    name = "TURBO MOTORS TV",
                    category = "Sport HD",
                    logoUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=300&q=80",
                    streamUrl = mp4WhatCarCanYouGet,
                    streamFormat = "MP4",
                    epgChannelId = "turbo.motors.tv",
                    currentProgramTitle = "Supercars & Essais Extrêmes",
                    currentProgramDesc = "Comparatif sur piste des sportives les plus emblématiques de l'année.",
                    currentProgramStart = "21:10",
                    currentProgramEnd = "22:15",
                    currentProgramProgress = 0.65f,
                    nextProgramTitle = "Rallye Raid Légendes",
                    nextProgramTime = "22:15 - 23:30",
                    hasCatchUp = false,
                    catchUpDays = 0
                ),
                LiveChannelEntity(
                    playlistId = playlistId,
                    channelNumber = 10,
                    name = "ORIENT FANTASY BOX",
                    category = "Cinéma & Séries",
                    logoUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=300&q=80",
                    streamUrl = mp4ElephantsDream,
                    streamFormat = "MP4",
                    epgChannelId = "orient.fantasy.box",
                    currentProgramTitle = "Elephants Dream : Le Labyrinthe Mécanique",
                    currentProgramDesc = "Deux personnages explorent les rouages infinis d'une machine surréaliste.",
                    currentProgramStart = "21:00",
                    currentProgramEnd = "22:15",
                    currentProgramProgress = 0.48f,
                    nextProgramTitle = "Nuit de l'Animation",
                    nextProgramTime = "22:15 - 00:00",
                    hasCatchUp = true,
                    catchUpDays = 3
                ),
                LiveChannelEntity(
                    playlistId = playlistId,
                    channelNumber = 11,
                    name = "HORIZON MUSIC LIVE",
                    category = "Musique & Concerts",
                    logoUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=300&q=80",
                    streamUrl = mp4ForBiggerFun,
                    streamFormat = "MP4",
                    epgChannelId = "horizon.music.live",
                    currentProgramTitle = "Festival Electro & Lounge Sessions",
                    currentProgramDesc = "Performances live enregistrées en 4K Dolby Audio.",
                    currentProgramStart = "21:00",
                    currentProgramEnd = "23:30",
                    currentProgramProgress = 0.35f,
                    nextProgramTitle = "Top Hits Acoustic",
                    nextProgramTime = "23:30 - 01:00",
                    hasCatchUp = false
                ),
                LiveChannelEntity(
                    playlistId = playlistId,
                    channelNumber = 12,
                    name = "ORIENT NIGHT CLUB (+18)",
                    category = "Adulte / Verrouillé",
                    logoUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=300&q=80",
                    streamUrl = mp4ForBiggerJoy,
                    streamFormat = "MP4",
                    epgChannelId = "orient.night.pin",
                    currentProgramTitle = "Programme Nocturne (Contrôle Parental Actif)",
                    currentProgramDesc = "Chaîne protégée par code PIN parental dans les paramètres d'Orient Player.",
                    currentProgramStart = "22:00",
                    currentProgramEnd = "02:00",
                    currentProgramProgress = 0.25f,
                    nextProgramTitle = "Nuit Cinéma",
                    nextProgramTime = "02:00 - 05:00",
                    isAdult = true
                )
            )
            dao.insertChannels(channels)

            // Seed VOD Movies
            val movies = listOf(
                VodMovieEntity(
                    playlistId = playlistId,
                    title = "Tears of Steel : Horizon 2084",
                    category = "Science-Fiction",
                    posterUrl = "https://images.unsplash.com/photo-1535016120720-40c646be5580?w=600&q=80",
                    backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&q=80",
                    streamUrl = hlsTearsOfSteel,
                    year = 2025,
                    genre = "Science-Fiction • Action • Cyberpunk",
                    rating = 8.9,
                    durationMinutes = 118,
                    director = "Ian Hubert",
                    cast = "Derek de Lint, Sergio Hasselbaink, Rogier Schipper",
                    description = "Dans une ville d'Amsterdam futuriste dominée par des dômes énergétiques et des synthétiques autonomes, un groupe de résistants tente une ultime opération pour restaurer l'équilibre humain.",
                    isFavorite = true,
                    resumePositionMs = 185_000L,
                    totalDurationMs = 734_000L,
                    lastWatchedMillis = now - 3_600_000L
                ),
                VodMovieEntity(
                    playlistId = playlistId,
                    title = "Sintel : Le Serment des Cimes",
                    category = "Fantastique & Aventure",
                    posterUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&q=80",
                    backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&q=80",
                    streamUrl = hlsSintel,
                    year = 2024,
                    genre = "Fantastique • Animation • Épopée",
                    rating = 9.1,
                    durationMinutes = 104,
                    director = "Colin Levy",
                    cast = "Halina Reijn, Thom Hoffman",
                    description = "Une quête épique et émouvante à travers les montagnes éternelles et les déserts de cendres. Sintel brave tous les dangers pour retrouver le dragon qu'elle a sauvé autrefois.",
                    isFavorite = true,
                    resumePositionMs = 95_000L,
                    totalDurationMs = 888_000L,
                    lastWatchedMillis = now - 7_200_000L
                ),
                VodMovieEntity(
                    playlistId = playlistId,
                    title = "Les Dunes d'Émeraude",
                    category = "Thriller & Action",
                    posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&q=80",
                    backdropUrl = "https://images.unsplash.com/photo-1512632578888-169bbbc64f33?w=1200&q=80",
                    streamUrl = mp4ForBiggerEscapes,
                    year = 2026,
                    genre = "Thriller • Espionnage • Aventure",
                    rating = 8.6,
                    durationMinutes = 132,
                    director = "Tarek El-Mansour",
                    cast = "Sami Bouajila, Leïla Bekhti, Adam Bessa",
                    description = "Un cryptographe international est appelé d'urgence entre Dubaï, Tanger et Genève lorsqu'un câble sous-marin stratégique fait l'objet d'un sabotage coordonné.",
                    isFavorite = false
                ),
                VodMovieEntity(
                    playlistId = playlistId,
                    title = "Big Buck : L'Empire de la Forêt",
                    category = "Famille & Animation",
                    posterUrl = "https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?w=600&q=80",
                    backdropUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?w=1200&q=80",
                    streamUrl = mp4BigBuckBunny,
                    year = 2023,
                    genre = "Animation • Comédie • Famille",
                    rating = 8.4,
                    durationMinutes = 92,
                    director = "Sacha Goedegebure",
                    cast = "Studio Blender Animation",
                    description = "Lorsque trois rongeurs espiègles troublent la paix de la vallée printanière, un lapin géant décide de leur donner une leçon mémorable pleine d'humour.",
                    isFavorite = false
                ),
                VodMovieEntity(
                    playlistId = playlistId,
                    title = "Le Protocole Machina",
                    category = "Science-Fiction",
                    posterUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&q=80",
                    backdropUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1200&q=80",
                    streamUrl = mp4ElephantsDream,
                    year = 2025,
                    genre = "Science-Fiction • Mystère",
                    rating = 8.2,
                    durationMinutes = 110,
                    director = "Bassam Kurdali",
                    cast = "Tygo Gernandt, Cas Jansen",
                    description = "Plongée vertigineuse au cœur d'une citadelle cybernétique où la frontière entre perception virtuelle et architecture physique s'efface.",
                    isFavorite = false
                ),
                VodMovieEntity(
                    playlistId = playlistId,
                    title = "Apex : Chrono 300 Km/h",
                    category = "Thriller & Action",
                    posterUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=600&q=80",
                    backdropUrl = "https://images.unsplash.com/photo-1541534741688-6078c6bfb5c5?w=1200&q=80",
                    streamUrl = mp4WeAreGoingOnBullrun,
                    year = 2026,
                    genre = "Action • Course • Drame",
                    rating = 8.7,
                    durationMinutes = 126,
                    director = "Marcus Vance",
                    cast = "Elena Rostova, Lucas Thorne",
                    description = "Des coulisses d'une écurie d'endurance jusqu'aux 24 Heures nocturnes sous la pluie, le duel acharné de deux pilotes rivaux.",
                    isFavorite = true
                )
            )
            dao.insertMovies(movies)

            // Seed Series & Multi-Season Episodes
            val s1Id = dao.insertSeries(
                SeriesEntity(
                    playlistId = playlistId,
                    title = "Citadelle de Nuit",
                    category = "Série Originale • Thriller",
                    posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&q=80",
                    backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&q=80",
                    year = 2026,
                    genre = "Cyber-Thriller • Espionnage",
                    rating = 9.3,
                    seasonsCount = 2,
                    description = "Une unité spéciale de cybersécurité traque un syndicat clandestin opérant à travers les satellites géostationnaires de Méditerranée.",
                    isFavorite = true
                )
            )
            val s2Id = dao.insertSeries(
                SeriesEntity(
                    playlistId = playlistId,
                    title = "Chroniques de l'Atlas",
                    category = "Aventure & Histoire",
                    posterUrl = "https://images.unsplash.com/photo-1512632578888-169bbbc64f33?w=600&q=80",
                    backdropUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1200&q=80",
                    year = 2025,
                    genre = "Aventure • Drame Historique",
                    rating = 8.8,
                    seasonsCount = 2,
                    description = "Les routes caravanières et les observatoires astronomiques d'Orient racontés à travers le destin de trois familles d'érudits et de navigateurs.",
                    isFavorite = true
                )
            )
            val s3Id = dao.insertSeries(
                SeriesEntity(
                    playlistId = playlistId,
                    title = "Paddock GP : Inside",
                    category = "Documentaire Sport",
                    posterUrl = "https://images.unsplash.com/photo-1541534741688-6078c6bfb5c5?w=600&q=80",
                    backdropUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=1200&q=80",
                    year = 2026,
                    genre = "Sport • Immersion",
                    rating = 9.0,
                    seasonsCount = 1,
                    description = "Accès total aux ingénieurs, pilotes et directeurs d'écuries durant toute la saison du championnat mondial.",
                    isFavorite = false
                )
            )

            val episodes = listOf(
                EpisodeEntity(
                    seriesId = s1Id,
                    seasonNumber = 1,
                    episodeNumber = 1,
                    title = "Épisode 1 : Signal Fantôme",
                    overview = "Un satellite de communication émet une séquence cryptée au-dessus du détroit de Gibraltar.",
                    durationMinutes = 52,
                    thumbnailUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&q=80",
                    streamUrl = hlsTearsOfSteel,
                    resumePositionMs = 120_000L,
                    totalDurationMs = 734_000L
                ),
                EpisodeEntity(
                    seriesId = s1Id,
                    seasonNumber = 1,
                    episodeNumber = 2,
                    title = "Épisode 2 : Protocole Bleu Nuit",
                    overview = "L'équipe remonte la trace du serveur maître jusqu'à un port franc automatisé.",
                    durationMinutes = 49,
                    thumbnailUrl = "https://images.unsplash.com/photo-1535016120720-40c646be5580?w=600&q=80",
                    streamUrl = hlsSintel
                ),
                EpisodeEntity(
                    seriesId = s1Id,
                    seasonNumber = 1,
                    episodeNumber = 3,
                    title = "Épisode 3 : Point de Bascule",
                    overview = "Une course contre la montre s'engage pour empêcher la coupure du réseau financier.",
                    durationMinutes = 55,
                    thumbnailUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&q=80",
                    streamUrl = mp4TearsOfSteel
                ),
                EpisodeEntity(
                    seriesId = s1Id,
                    seasonNumber = 2,
                    episodeNumber = 1,
                    title = "S2 Épisode 1 : Horizon Zéro",
                    overview = "Six mois plus tard, une nouvelle architecture quantique réactive la cellule d'enquête.",
                    durationMinutes = 58,
                    thumbnailUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&q=80",
                    streamUrl = mp4ForBiggerBlazes
                ),
                EpisodeEntity(
                    seriesId = s1Id,
                    seasonNumber = 2,
                    episodeNumber = 2,
                    title = "S2 Épisode 2 : La Clé d'Orient",
                    overview = "Révélation finale au sommet du centre de données orbital.",
                    durationMinutes = 61,
                    thumbnailUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&q=80",
                    streamUrl = mp4ForBiggerEscapes
                ),
                EpisodeEntity(
                    seriesId = s2Id,
                    seasonNumber = 1,
                    episodeNumber = 1,
                    title = "Épisode 1 : Les Cartographes des Étoiles",
                    overview = "À Samarcande et Cordoue, les astronomes dressent la carte céleste qui changera la navigation.",
                    durationMinutes = 48,
                    thumbnailUrl = "https://images.unsplash.com/photo-1512632578888-169bbbc64f33?w=600&q=80",
                    streamUrl = mp4Sintel
                ),
                EpisodeEntity(
                    seriesId = s2Id,
                    seasonNumber = 1,
                    episodeNumber = 2,
                    title = "Épisode 2 : La Traversée des Vents",
                    overview = "Une expédition maritime affronte les courants de l'océan Indien guidée par l'astrolabe royal.",
                    durationMinutes = 51,
                    thumbnailUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&q=80",
                    streamUrl = mp4ForBiggerEscapes
                ),
                EpisodeEntity(
                    seriesId = s2Id,
                    seasonNumber = 2,
                    episodeNumber = 1,
                    title = "S2 Épisode 1 : La Bibliothèque d'Or",
                    overview = "La sauvegarde des manuscrits anciens face aux tempêtes du désert.",
                    durationMinutes = 54,
                    thumbnailUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&q=80",
                    streamUrl = mp4ElephantsDream
                ),
                EpisodeEntity(
                    seriesId = s3Id,
                    seasonNumber = 1,
                    episodeNumber = 1,
                    title = "Épisode 1 : Départ sous Tension",
                    overview = "Les essais hivernaux révèlent des écarts infimes entre les trois favoris.",
                    durationMinutes = 45,
                    thumbnailUrl = "https://images.unsplash.com/photo-1541534741688-6078c6bfb5c5?w=600&q=80",
                    streamUrl = mp4WeAreGoingOnBullrun
                ),
                EpisodeEntity(
                    seriesId = s3Id,
                    seasonNumber = 1,
                    episodeNumber = 2,
                    title = "Épisode 2 : Stratégie Nocturne",
                    overview = "Pluie torrentielle et arrêt au stand décisif dans le dernier tour.",
                    durationMinutes = 47,
                    thumbnailUrl = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=600&q=80",
                    streamUrl = mp4WhatCarCanYouGet
                )
            )
            dao.insertEpisodes(episodes)

            // Seed Catch-Up / Replay Broadcasts
            val catchUpList = listOf(
                CatchUpProgramEntity(
                    channelId = 1,
                    channelName = "ORIENT SPORT 1 UHD",
                    channelLogo = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=300&q=80",
                    category = "Sport HD",
                    title = "Quart de Finale Aller : Résumé Long & Analyses 4K",
                    description = "Revivez l'intégralité des temps forts, buts et analyses tactiques au ralenti.",
                    dayLabel = "Aujourd'hui",
                    timeSlot = "18:30 - 20:30",
                    durationMinutes = 120,
                    streamUrl = hlsTearsOfSteel
                ),
                CatchUpProgramEntity(
                    channelId = 2,
                    channelName = "ORIENT CINÉMA PREMIER",
                    channelLogo = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=300&q=80",
                    category = "Cinéma & Séries",
                    title = "Grand Écran : Sintel Édition Collector",
                    description = "Diffusion spéciale suivie de l'entretien avec l'équipe de réalisation.",
                    dayLabel = "Aujourd'hui",
                    timeSlot = "16:00 - 17:45",
                    durationMinutes = 105,
                    streamUrl = hlsSintel
                ),
                CatchUpProgramEntity(
                    channelId = 4,
                    channelName = "ATLAS DISCOVERY 4K",
                    channelLogo = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=300&q=80",
                    category = "Documentaires",
                    title = "Les Sanctuaires de l'Himalaya",
                    description = "Expédition haute altitude filmée par drone en Ultra HD.",
                    dayLabel = "Hier",
                    timeSlot = "20:50 - 22:20",
                    durationMinutes = 90,
                    streamUrl = mp4ForBiggerEscapes
                ),
                CatchUpProgramEntity(
                    channelId = 8,
                    channelName = "AL-SHARQ CULTURE HD",
                    channelLogo = "https://images.unsplash.com/photo-1512632578888-169bbbc64f33?w=300&q=80",
                    category = "Monde & Orient",
                    title = "Nuit Andalouse : Concert philharmonique",
                    description = "Enregistrement exceptionnel en direct du Palais des Arts.",
                    dayLabel = "Hier",
                    timeSlot = "21:00 - 23:00",
                    durationMinutes = 120,
                    streamUrl = mp4ForBiggerBlazes
                ),
                CatchUpProgramEntity(
                    channelId = 6,
                    channelName = "ORIENT KIDS & TOONS",
                    channelLogo = "https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?w=300&q=80",
                    category = "Jeunesse",
                    title = "Big Buck Bunny : Spécial Week-End",
                    description = "Épisode intégral en haute définition pour toute la famille.",
                    dayLabel = "Il y a 2 jours",
                    timeSlot = "10:00 - 11:30",
                    durationMinutes = 90,
                    streamUrl = mp4BigBuckBunny
                )
            )
            dao.insertCatchUpPrograms(catchUpList)
        }
    }
}
