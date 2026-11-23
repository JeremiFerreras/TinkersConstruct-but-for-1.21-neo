"""Exercise the packaged mod on a fresh world and after reopening it."""
import pathlib
import subprocess

root = pathlib.Path.cwd()
(root / "eula.txt").write_text("eula=true\n")
(root / "server.properties").write_text("server-ip=127.0.0.1\nlevel-seed=1729\nview-distance=3\nsimulation-distance=3\n")
for attempt in range(2):
    process = subprocess.Popen(["bash", "run.sh", "nogui"], stdin=subprocess.PIPE,
                               stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    def command(value):
        print("> " + value, flush=True)
        process.stdin.write(value + "\n")
        process.stdin.flush()

    def expect(fragment):
        for line in process.stdout:
            print(line.rstrip(), flush=True)
            if fragment in line:
                return
        raise RuntimeError("Server exited before " + fragment)

    try:
        expect('For help, type "help"')
        command("forceload add 0 0")
        if attempt == 0:
            command("setblock 0 100 0 tconstruct:tinker_station")
        command("execute if block 0 100 0 tconstruct:tinker_station run say STATION_OK")
        expect("[Server] STATION_OK")
        command("summon minecraft:item 0 101 0 {Item:{id:'tconstruct:slime_boots',count:1}}")
        command("execute if entity @e[type=item,nbt={Item:{id:'tconstruct:slime_boots'}}] run say BOOTS_OK")
        expect("[Server] BOOTS_OK")
        if attempt == 0:
            command("reload")
            expect("Loaded")
            command("save-all flush")
            expect("Saved the game")
        command("stop")
        output, _ = process.communicate(timeout=120)
        print(output)
        if process.returncode:
            raise RuntimeError("Server failed to stop cleanly")
    finally:
        if process.poll() is None:
            process.kill()
