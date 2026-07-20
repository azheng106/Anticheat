## AntiCheat Plugin - Minecraft 🚀

Basic `1.8x` plugin to detect cheaters in Minecraft Java Edition using the [PacketEvents](https://docs.packetevents.com/) library.

> 📖 The original YouTube series can be found on [YouTube](https://youtu.be/2g_8jg9vw3Q)

## Supported Checks 🏹
The following checks below have been implemented:

| ⚔️ Combat | 📦 Packet | 🏃 Movement |
|-----------|-----------|-------------|
| AimA | Timer | NoFallA |
| KillauraA | Blink | NoFallB |
| KillauraB | Invalid Pitch | SpeedA |
| ReachA | PingSpoof | |
| ReachB | | |

## Check Explanations ❓
Below check explanations for the first few episodes (the later episodes go more in-depth into explanation) can be found:

**KillauraA:**
- In vanilla Minecraft (`1.8x`), when a player moves or looks around, the client sends packets such as:
  - `PLAYER_FLYING`
  - `PLAYER_POSITION`
  - `PLAYER_ROTATION`
  to update its position and orientation.

- When players attack, the client sends an `INTERACT_ENTITY` packet. Due to:
  - Human reaction times
  - Network latency (lag)
  - The game's tick rate

  there is usually a *delay* of more than *a few milliseconds* between a movement update (`PLAYER_FLYING`) and an attack (`INTERACT_ENTITY`) packet.

- Many killaura cheats are designed to send attack packets almost immediately after the last movement update.
- This creates an abnormally short interval between:
  - The last `PLAYER_FLYING` packet
  - The `INTERACT_ENTITY` packet
- Our check detects this abnormally short interval to identify potential killaura behavior.

**NoFall (A):**
- Cheat clients can avoid taking fall damage by telling the server that they are on the ground, even when they aren't.
  - This is done by using the `clientGround` state.

- As the server, we can calculate a `serverGround` state by looking at the player's `y` position.
  - Finding a mismatch between `clientGround` and `serverGround` (while maintaining a buffer and `1-tick` desync to avoid false flags) allows us to catch the cheater.

**NoFall (B):**
- Check for the default behavior of taking fall damage when falling from over 3 blocks.
   - If the player doesn't take fall damage and didn't land on a soft block like slime or water, then flag them.

**Why does NoFall (A) detect Flight hacks sometimes?**
- When flying off a solid block, our `serverGround` calculation (which checks if player's Y % 1/64 roughly = 0) remains `true`, while the `clientGround` becomes `false` because the player is now flying in the air. This causes `NoFall (A)` to flag.
- When falling a bit or jumping first before beginning to fly, `NoFall (A)` is not triggered because the player's Y becomes a weird decimal, so `serverGround` becomes `false`, which matches the `clientGround`. Thus, `NoFall (A)` is not an actual robust fly check, but the fly detection is simply a weird side effect of how we did our check.
