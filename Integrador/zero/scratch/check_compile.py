import glob, subprocess, os, sys

os.makedirs('scratch/classes', exist_ok=True)
os.makedirs('scratch/test-classes', exist_ok=True)

m2_jars = [j for j in glob.glob('/home/augusto/.m2/repository/**/*.jar', recursive=True) 
           if not j.endswith('-sources.jar') and not j.endswith('-javadoc.jar') and '6.0.3' not in j]

classpath_main = ':'.join(['scratch/classes'] + m2_jars)
sources_main = glob.glob('src/main/java/**/*.java', recursive=True)

opens = [
    '--add-modules', 'jdk.unsupported',
    '--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED',
    '--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED',
    '--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED',
    '--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED',
    '--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED',
    '--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED',
    '--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED',
    '--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED',
    '--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED',
    '--add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED'
]

print("Compiling main sources...")
cmd_main = ['/usr/lib/jvm/java-21-openjdk-amd64/bin/java', '--module-path', '/usr/games/tlauncher/lib/jvm/jre/jmods'] + opens + [
    '-m', 'jdk.compiler/com.sun.tools.javac.Main',
    '-processorpath', '/home/augusto/.m2/repository/org/projectlombok/lombok/1.18.32/lombok-1.18.32.jar',
    '-cp', classpath_main,
    '-d', 'scratch/classes'
] + sources_main

res_main = subprocess.run(cmd_main, capture_output=True, text=True)
if res_main.returncode != 0:
    print('Main Errors:\n', res_main.stderr)
    print(res_main.stdout)
else:
    print("Main compiled successfully.")

print("Compiling test sources...")
classpath_test = ':'.join(['scratch/classes', 'scratch/test-classes'] + m2_jars)
sources_test = [f for f in glob.glob('src/test/java/**/*.java', recursive=True) if 'ZeroApplicationTests' not in f]

cmd_test = ['/usr/lib/jvm/java-21-openjdk-amd64/bin/java', '--module-path', '/usr/games/tlauncher/lib/jvm/jre/jmods'] + opens + [
    '-m', 'jdk.compiler/com.sun.tools.javac.Main',
    '-cp', classpath_test,
    '-d', 'scratch/test-classes'
] + sources_test

res_test = subprocess.run(cmd_test, capture_output=True, text=True)
if res_test.returncode != 0:
    print('Test Compile Errors:\n', res_test.stderr)
    print(res_test.stdout)
else:
    print("Tests compiled successfully.")

