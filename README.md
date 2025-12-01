YouTube series: https://youtu.be/2g_8jg9vw3Q

Check explanations:

KillauraA:

In vanilla Minecraft (1.8), when a player moves or looks around, the client sends packets like PLAYER_FLYING, PLAYER_POSITION, or PLAYER_ROTATION to update its position and orientation. When players attack, the client sends an INTERACT_ENTITY packet. Due to human reaction times, lag, and game's tick rate, there is usually a delay of more than a few milliseconds between a movement update (flying packet) and an attack (INTERACT_ENTITY) packet. Many killauras are designed to send attack packets almost immediately after last movement update, which creates an abnormally short interval between the last flying packet and the INTERACT_ENTITY packet. Our check is detecting this abnormally short interval.
