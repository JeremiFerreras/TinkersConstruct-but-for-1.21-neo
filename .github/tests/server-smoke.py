"""Exercise the packaged mod on a fresh world and after reopening it."""
import pathlib
import signal
import time
import subprocess

root = pathlib.Path.cwd()
(root / "eula.txt").write_text("eula=true\n")
(root / "server.properties").write_text("server-ip=127.0.0.1\nlevel-seed=1729\nview-distance=3\nsimulation-distance=3\n")
def timeout(signum, frame):
    raise TimeoutError("Timed out waiting for the dedicated server")

signal.signal(signal.SIGALRM, timeout)
for attempt in range(2):
    signal.alarm(240)
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
        if attempt == 0:
            command("setblock 1 100 0 minecraft:chest")
            command("item replace block 1 100 0 container.0 with tconstruct:slime_boots")
        command("execute if items block 1 100 0 container.0 tconstruct:slime_boots run say BOOTS_OK")
        expect("[Server] BOOTS_OK")
        if attempt == 0:
            command("setblock 2 100 0 minecraft:furnace")
            command("item replace block 2 100 0 container.0 with tconstruct:grout")
            command("item replace block 2 100 0 container.1 with minecraft:coal")
            for _ in range(30):
                time.sleep(1)
                command("execute if items block 2 100 0 container.2 tconstruct:seared_brick run say SMELT_OK")
                command("say POLL_END")
                found = False
                for line in process.stdout:
                    print(line.rstrip(), flush=True)
                    if "[Server] SMELT_OK" in line:
                        found = True
                    if "[Server] POLL_END" in line:
                        break
                if found:
                    break
            else:
                raise AssertionError("Grout did not smelt into seared brick")
            command("setblock 4 100 1 tconstruct:seared_basin")
            command('setblock 3 101 1 tconstruct:seared_ingot_tank{tank:{Fluid:{id:"tconstruct:molten_copper",amount:810}}}')
            command("setblock 4 101 1 tconstruct:seared_faucet[facing=east]")
            command("setblock 4 101 2 minecraft:redstone_block")
            command("tick sprint 1500")
            expect("Sprint completed")
            command('execute if data block 4 100 1 Items[{Slot:1b,id:"minecraft:copper_block"}] run say CAST_OK')
            expect("[Server] CAST_OK")
            command("locate structure tconstruct:earth_slime_island")
            expect("The nearest tconstruct:earth_slime_island is at")
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
