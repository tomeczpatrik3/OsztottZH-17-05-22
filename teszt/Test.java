import static java.nio.file.Files.readAllLines;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import alapfeladat.KartyaSzerver;
import alapfeladat.KartyaJatekos;

public class Test {

	private static final int PLAYER_COUNT = 4;

	public static void main(String[] args) throws Exception {
		new Test().run();
	}

	private void run() throws InterruptedException, IOException {
		// std out atiranyitasa
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final FileOutputStream stdout = new FileOutputStream(FileDescriptor.out);
		System.setOut(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				baos.write(b);
				stdout.write(b);
			}
		}));

		// szerver elinditasa
		runMain(1, args -> KartyaSzerver.main(args), n -> "", "KSzerver");

		sleepMsec(100);

		// kliensek elinditasa
		runMain(PLAYER_COUNT, args -> KartyaJatekos.main(args), n -> "Jatekos" + (n + 1) + " input" + (n + 1) + ".txt " + (5-n),
				"KJatekos");

		waitForMainsToFinish();

		compareWithExpectedFile(baos.toString(), "output.txt");

		System.setOut(new PrintStream(stdout));
	}

	private List<Thread> mainThreads = new ArrayList<>();

	public void waitForMainsToFinish() throws InterruptedException {
		for (Thread thread : mainThreads) {
			thread.join();
		}

		mainThreads = new ArrayList<>();
	}

	public void runMain(int count, ConsumerEx<String[]> main, Function<Integer, String> getArgsTxt,
			String threadNamePrefix) {
		for (int i = 0; i < count; ++i) {
			String[] serverArgs = getArgsTxt.apply(i).split(" ");

			String threadName = threadNamePrefix + (count == 1 ? "" : i);

			Thread thread = new Thread(withoutException(() -> main.accept(serverArgs)), threadName);
			thread.start();

			mainThreads.add(thread);

			sleepMsec(200);
		}
	}

	public void compareWithExpectedFile(String output, String expectedFilename) throws IOException {
		List<String> lines1 = Arrays.asList(output.split("\n"));
		List<String> lines2 = readAllLines(Paths.get(expectedFilename));
		if (lines1.size() != lines2.size()) {
			System.err.println("A kimenet nem ugyanannyi sort tartalmaz, mint a megadott mintakimenet.");
			return;
		}

		for (int i = 0; i < lines1.size(); i++) {
			if (!lines1.get(i).trim().equals(lines2.get(i))) {
				System.err.println("A " + (i + 1) + ". sora a kimenetnek elter a mintakimenettol.");
				System.err.println("\tKimenet: " + lines1.get(i).trim());
				System.err.println("\tMintakimenet: " + lines2.get(i));
				return;
			}
		}
        System.out.println("A teszt sikeres.");
	}

	private static void sleepMsec(int msec) {
		try {
			Thread.sleep(msec);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static Runnable withoutException(RunnableEx r) {
		return () -> {
			try {
				r.run();
			} catch (Exception e) {
				synchronized (System.err) {
					System.err.println(
							"A futas soran hiba lepett fel a(z) " + Thread.currentThread().getName() + " szalban:");
					e.printStackTrace(System.err);
				}
			}
		};
	}

	public static <T> Consumer<T> withoutException(ConsumerEx<T> c) {
		return args -> withoutException(() -> c.accept(args));
	}

	interface ConsumerEx<T> {
		public void accept(T t) throws Exception;
	}

	interface RunnableEx {
		public void run() throws Exception;
	}

}
