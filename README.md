# LivemodeOBS (BETA) - Minecraft OBS Camera Mod 

This is a Minecraft mod that integrates with OBS (Open Broadcaster Software) to provide a camera control system in-game, allowing users to switch between different camera angles and scenes. It provides an easy-to-use interface and allows seamless integration with OBS for live streaming.

## Features
- **Camera Setup**: Set custom camera positions in-game with `/setcam <name>`.
- **Camera Management**: View and delete cameras with `/cam <name>` and `/delcam <name>`.
- **OBS Scene Switcher**: Switch to a scene in OBS with `/obsswitch <name>`.
- **OBS Connection**: Connect to OBS WebSocket with `/obsconnect <ip> <port>`.
- **Camera Teleportation**: When switching scenes, the player can be teleported to the camera position automatically.

## Requirements
- Minecraft version 1.21.4
- Fabric Mod Loader
- OBS WebSocket plugin for OBS (Ensure it's set up before connecting)

## Installation

1. Download the latest release of the mod.
2. Place the `.jar` file in the `mods` folder in your Minecraft directory.
3. Install Fabric if you haven't already.
4. Install the OBS WebSocket plugin and ensure it's running on your OBS instance.

## Commands
- `/setcam <name>`: Set a camera position at your current location.
- `/cam <name>`: View the position of a camera.
- `/delcam <name>`: Delete a camera.
- `/obsswitch <name>`: Switch the OBS scene to a specific scene.
- `/obsconnect <ip> <port>`: Connect to an OBS WebSocket server.
- `/obscontrol`: Set yourself to be controlled by OBS scenes (teleports to cameras based on the scene).

## Usage
After setting up cameras and connecting to OBS, use the commands to control the camera view in your Minecraft world. When a scene switch happens in OBS, your player will be automatically teleported to the corresponding camera position.

## Contributing
Feel free to open an issue or submit a pull request if you'd like to contribute improvements, bug fixes, or new features!

## License
This mod is licensed under the MIT License.
