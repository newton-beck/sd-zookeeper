package br.edu.ufabc.zookeeper;

import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

import br.edu.ufabc.zookeeper.util.Nos;

public class Produtor implements Runnable, Watcher {

	public static void main(String[] args) throws Exception {
		new Thread(new Produtor()).start();
	}

	private ZooKeeper zooKeeper;

	private String meuNo;

	private boolean souLider;

	public Produtor() throws Exception {
		zooKeeper = new ZooKeeper("localhost", 3_000, this);

		// Cria seu nó para leader election
		meuNo = zooKeeper.create("/leaders/node", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		System.out.println("Nó " + meuNo + " para eleição criado com sucesso.");

		// Registra o watch no diretório de leaders
		List<String> nos = zooKeeper.getChildren("/leaders", true);

		// Se não tem nenhum nó ele vira líder
		souLider = nos.isEmpty();
	}

	@Override
	public void run() {
		while (true) {

		}
	}

	@Override
	public void process(WatchedEvent event) {
		if (EventType.NodeChildrenChanged == event.getType()) {
			System.out.println("Iniciando a eleição...");
			try {
				// Espera até o nó antigo sair do diretório
				Thread.sleep(3_000);
				List<String> nos = zooKeeper.getChildren("/leaders", true);
				String noMaisVelho = Nos.maisVelho(nos);
				System.out.println("O nó mais velho é " + noMaisVelho);
				if (meuNo.endsWith(noMaisVelho)) {
					System.err.println("Venci a eleição! Chegou minha vez :)");
					souLider = true;
				} else {
					System.out.println("Ainda não foi dessa vez :(");
					souLider = false;
				}
			} catch (KeeperException | InterruptedException e) {
				// TODO O que fazer?
			}
		}
	}

}
