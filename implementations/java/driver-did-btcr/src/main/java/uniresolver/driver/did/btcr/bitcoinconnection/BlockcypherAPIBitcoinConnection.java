package uniresolver.driver.did.btcr.bitcoinconnection;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import info.weboftrust.txrefconversion.TxrefConverter.Chain;

public class BlockcypherAPIBitcoinConnection extends info.weboftrust.txrefconversion.bitcoinconnection.BlockcypherAPIBitcoinConnection implements BitcoinConnection {

	private static final BlockcypherAPIBitcoinConnection instance = new BlockcypherAPIBitcoinConnection();

	public BlockcypherAPIBitcoinConnection() {

	}

	public static BlockcypherAPIBitcoinConnection get() {

		return instance;
	}

	@Override
	public BtcrData getBtcrData(Chain chain, String txid) throws IOException {

		// retrieve transaction data

		URI uri;
		if (chain == Chain.MAINNET) {
			uri = URI.create("https://api.blockcypher.com/v1/btc/main/txs/" + txid + "?limit=500");
		} else {
			uri = URI.create("https://api.blockcypher.com/v1/btc/test3/txs/" + txid + "?limit=500");
		}

		JsonObject txData = retrieveJson(uri);

		// find input script pub key

		String inputScriptPubKey = null;

		for (Iterator<JsonElement> i = ((JsonArray) txData.get("inputs")).iterator(); i.hasNext(); ) {

			JsonObject input = i.next().getAsJsonObject();
			JsonElement script = input.get("script");
			JsonElement scriptType = input.get("script_type");
			if (script == null || ! script.isJsonPrimitive()) continue;
			if (scriptType == null || ! scriptType.isJsonPrimitive()) continue;

			if ("pay-to-pubkey-hash".equals(scriptType.getAsString())) {

				Script payToPubKeyHashScript;

				try {

					payToPubKeyHashScript = new Script(Hex.decodeHex(script.getAsString().toCharArray()));
				} catch (ScriptException | DecoderException ex) {

					throw new IOException("Cannot decode script " + script.getAsString() + ": " + ex.getMessage(), ex);
				}

				inputScriptPubKey = Hex.encodeHexString(payToPubKeyHashScript.getPubKey());
				break;
			}
		}

		if (inputScriptPubKey == null) return null;

		// find more DDO URI

		URI moreDdoUri = null;

		for (Iterator<JsonElement> i = ((JsonArray) txData.get("outputs")).iterator(); i.hasNext(); ) {

			JsonObject output = i.next().getAsJsonObject();
			JsonElement script = output.get("script");
			JsonElement scriptType = output.get("script_type");
			if (script == null || ! script.isJsonPrimitive()) continue;
			if (scriptType == null || ! scriptType.isJsonPrimitive()) continue;

			if ("null-data".equals(scriptType.getAsString())) {

				Script nullDataScript;

				try {

					nullDataScript = new Script(Hex.decodeHex(script.getAsString().toCharArray()));
				} catch (ScriptException | DecoderException ex) {

					throw new IOException("Cannot decode script " + script.getAsString() + ": " + ex.getMessage(), ex);
				}

				ScriptChunk scriptChunk = nullDataScript.getChunks().size() == 2 ? nullDataScript.getChunks().get(1) : null;
				byte[] data = scriptChunk == null ? null : scriptChunk.data;

				if (data == null || data.length < 1) throw new IOException("Cannot find data in script " + script.getAsString());

				moreDdoUri = URI.create(new String(data, StandardCharsets.UTF_8));
				break;
			}
		}

		if (moreDdoUri == null) return null;

		// done

		return new BtcrData(inputScriptPubKey, moreDdoUri);
	}
}
