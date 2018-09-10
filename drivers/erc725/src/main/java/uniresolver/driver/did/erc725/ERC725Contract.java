package uniresolver.driver.did.erc725;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple6;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import rx.Observable;
import rx.functions.Func1;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version none.
 */
public class ERC725Contract extends Contract {
    private static final String BINARY = "6060604052600060065534156200001557600080fd5b62000030336001640100000000620014046200003682021704565b620001a3565b600160a060020a038216600090815260208190526040902054156200005a57600080fd5b8082600160a060020a03167f7d958a859734aa5212d2568f8700fe77619bc93d5b08abf1445585bac8bff60660405160405180910390a3600160a060020a038216600090815260208181526040808320849055838352600290915280822054916004918590859051600160a060020a03929092166c010000000000000000000000000282526014820152603401604051908190039020815260208082019290925260409081016000908120939093558383526002909152902080546001810162000125838262000153565b5060009182526020909120018054600160a060020a031916600160a060020a03939093169290921790915550565b8154818355818115116200017a576000838152602090206200017a9181019083016200017f565b505050565b620001a091905b808211156200019c576000815560010162000186565b5090565b90565b6116c580620001b36000396000f3006060604052600436106100ae5763ffffffff7c01000000000000000000000000000000000000000000000000000000006000350416630607f93781146100b357806341cbfc7b146101b15780634b24fd0d1461021a5780634ec79937146102395780634eee424a1461026857806369e784991461027e5780636fa282491461029d578063a2d39bdb146102b3578063b53ea1b6146102d5578063b61d27f6146102fa578063c9100bcb1461035f575b600080fd5b34156100be57600080fd5b61019f600480359060248035600160a060020a0316916044359160849060643590810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284378201915050505050509190803590602001908201803590602001908080601f01602080910402602001604051908101604052818152929190602084018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405281815292919060208401838380828437509496506104e695505050505050565b60405190815260200160405180910390f35b34156101bc57600080fd5b6101c76004356106b0565b60405160208082528190810183818151815260200191508051906020019060200280838360005b838110156102065780820151838201526020016101ee565b505050509050019250505060405180910390f35b341561022557600080fd5b61019f600160a060020a036004351661072c565b341561024457600080fd5b6102546004356024351515610747565b604051901515815260200160405180910390f35b341561027357600080fd5b610254600435610848565b341561028957600080fd5b610254600160a060020a0360043516610bf7565b34156102a857600080fd5b6101c7600435610c43565b34156102be57600080fd5b610254600160a060020a0360043516602435610cb4565b34156102e057600080fd5b610254600160a060020a0360043581169060243516610d01565b341561030557600080fd5b61019f60048035600160a060020a03169060248035919060649060443590810190830135806020601f82018190048102016040519081016040528181529291906020840183838082843750949650610d6895505050505050565b341561036a57600080fd5b610375600435610f83565b6040518087815260200186600160a060020a0316600160a060020a03168152602001858152602001806020018060200180602001848103845287818151815260200191508051906020019080838360005b838110156103de5780820151838201526020016103c6565b50505050905090810190601f16801561040b5780820380516001836020036101000a031916815260200191505b50848103835286818151815260200191508051906020019080838360005b83811015610441578082015183820152602001610429565b50505050905090810190601f16801561046e5780820380516001836020036101000a031916815260200191505b50848103825285818151815260200191508051906020019080838360005b838110156104a457808201518382015260200161048c565b50505050905090810190601f1680156104d15780820380516001836020036101000a031916815260200191505b50995050505050505050505060405180910390f35b600160a060020a03331660009081526020819052604081205460031461050b57600080fd5b8587604051600160a060020a03929092166c0100000000000000000000000002825260148201526034016040518091039020905060c06040519081016040908152888252600160a060020a038816602080840191909152818301889052606083018790526080830186905260a08301859052600084815260019091522081518155602082015160018201805473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a0392909216919091179055604082015181600201556060820151816003019080516105e492916020019061152b565b506080820151816004019080516105ff92916020019061152b565b5060a08201518160050190805161061a92916020019061152b565b505050600087815260036020526040808220549160049189908b9051600160a060020a03929092166c010000000000000000000000000282526014820152603401604051908190039020815260208082019290925260409081016000908120939093558983526003909152902080546001810161069783826115a9565b5060009182526020909120018190559695505050505050565b6106b86115d2565b6002600083815260200190815260200160002080548060200260200160405190810160405280929190818152602001828054801561071f57602002820191906000526020600020905b8154600160a060020a03168152600190910190602001808311610701575b505050505090505b919050565b600160a060020a031660009081526020819052604090205490565b600160a060020a03331660009081526020819052604081205460011461076c57600080fd5b6006546000848152600560205260409020600301541461078b57600080fd5b6006805460010190558115610842576000838152600560205260409081902080546001820154600160a060020a03909116929091600201905180828054600181600116156101000203166002900480156108265780601f106107fb57610100808354040283529160200191610826565b820191906000526020600020905b81548152906001019060200180831161080957829003601f168201915b505091505060006040518083038185876187965a03f193505050505b92915050565b60006108526115e4565b600083815260016020526040808220829182919060c090519081016040529081600082015481526020016001820160009054906101000a9004600160a060020a0316600160a060020a0316600160a060020a0316815260200160028201548152602001600382018054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561094d5780601f106109225761010080835404028352916020019161094d565b820191906000526020600020905b81548152906001019060200180831161093057829003601f168201915b50505050508152602001600482018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156109ef5780601f106109c4576101008083540402835291602001916109ef565b820191906000526020600020905b8154815290600101906020018083116109d257829003601f168201915b50505050508152602001600582018054600181600116156101000203166002900480601f016020809104026020016040519081016040528092919081815260200182805460018160011615610100020316600290048015610a915780601f10610a6657610100808354040283529160200191610a91565b820191906000526020600020905b815481529060010190602001808311610a7457829003601f168201915b50505050508152505093508360200151600160a060020a031633600160a060020a03161480610ad85750600160a060020a0333166000908152602081905260409020546001145b80610af4575030600160a060020a031633600160a060020a0316145b1515610aff57600080fd5b60008681526004602052604081208054908290559350600390855181526020810191909152604001600020805490925082906000198101908110610b3f57fe5b9060005260206000209001549050808284815481101515610b5c57fe5b60009182526020808320909101929092558281526004825260408082208690558882526001928390528120818155918201805473ffffffffffffffffffffffffffffffffffffffff1916905560028201819055610bbc6003830182611635565b610bca600483016000611635565b610bd8600583016000611635565b50508154610bea8360001983016115a9565b5060019695505050505050565b600160a060020a03331660009081526020819052604081205460011480610c2f575030600160a060020a031633600160a060020a0316145b1515610c3a57600080fd5b61072782611222565b610c4b6115d2565b6003600083815260200190815260200160002080548060200260200160405190810160405280929190818152602001828054801561071f57602002820191906000526020600020905b81548152600190910190602001808311610c945750505050509050919050565b600160a060020a03331660009081526020819052604081205460011480610cec575030600160a060020a031633600160a060020a0316145b1515610cf757600080fd5b6108428383611404565b600160a060020a03331660009081526020819052604081205460011480610d39575030600160a060020a031633600160a060020a0316145b1515610d4457600080fd5b610d5682610d518561072c565b611404565b610d5f83611222565b50600192915050565b600160a060020a0333166000908152602081905260408120546001811480610d905750600281145b1515610d9b57600080fd5b8484846006546040516c01000000000000000000000000600160a060020a038616028152601481018490526034810183805190602001908083835b60208310610df55780518252601f199092019160209182019101610dd6565b6001836020036101000a03801982511681845116179092525050509190910192835250506020019250604091505051908190039020915083600160a060020a038616837fce0206f766cbb69a1ad1b8485f947bf53c2c0a2f3cf1078b31bb424833d3b0fb8660405160208082528190810183818151815260200191508051906020019080838360005b83811015610e96578082015183820152602001610e7e565b50505050905090810190601f168015610ec35780820380516001836020036101000a031916815260200191505b509250505060405180910390a460806040519081016040908152600160a060020a03871682526020808301879052818301869052600654606084015260008581526005909152208151815473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a039190911617815560208201518160010155604082015181600201908051610f5892916020019061152b565b506060820151600390910155506001811415610f7b57610f79826001610747565b505b509392505050565b6000806000610f906115d2565b610f986115d2565b610fa06115d2565b610fa86115e4565b600088815260016020526040908190209060c090519081016040529081600082015481526020016001820160009054906101000a9004600160a060020a0316600160a060020a0316600160a060020a0316815260200160028201548152602001600382018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156110a05780601f10611075576101008083540402835291602001916110a0565b820191906000526020600020905b81548152906001019060200180831161108357829003601f168201915b50505050508152602001600482018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156111425780601f1061111757610100808354040283529160200191611142565b820191906000526020600020905b81548152906001019060200180831161112557829003601f168201915b50505050508152602001600582018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156111e45780601f106111b9576101008083540402835291602001916111e4565b820191906000526020600020905b8154815290600101906020018083116111c757829003601f168201915b5050505050815250509050806000015181602001518260400151836060015184608001518560a00151949d939c50919a509850965090945092505050565b600160a060020a03811660008181526020819052604080822054928291829182918691907fe96ba5805e91ce4b5225d90ad1aac15c207472188f51f24025974341360f0f8a905160405180910390a360026000868152602001908152602001600020935030600160a060020a031633600160a060020a0316141580156112a85750600185145b80156112b5575083546001145b156112bf57600080fd5b8585604051600160a060020a03929092166c01000000000000000000000000028252601482015260340160405190819003902060008181526004602052604081208054919055855491945092508490600019810190811061131c57fe5b6000918252602090912001548454600160a060020a039091169150819085908490811061134557fe5b6000918252602080832091909101805473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a0394851617905591831681529081905260408082205484926004929091859151600160a060020a03929092166c010000000000000000000000000282526014820152603401604051908190039020815260208101919091526040016000205583546113e28560001983016115a9565b505050600160a060020a03909316600090815260208190526040812055505050565b600160a060020a0382166000908152602081905260409020541561142757600080fd5b8082600160a060020a03167f7d958a859734aa5212d2568f8700fe77619bc93d5b08abf1445585bac8bff60660405160405180910390a3600160a060020a038216600090815260208181526040808320849055838352600290915280822054916004918590859051600160a060020a03929092166c01000000000000000000000000028252601482015260340160405190819003902081526020808201929092526040908101600090812093909355838352600290915290208054600181016114f083826115a9565b506000918252602090912001805473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a03939093169290921790915550565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061156c57805160ff1916838001178555611599565b82800160010185558215611599579182015b8281111561159957825182559160200191906001019061157e565b506115a592915061167c565b5090565b8154818355818115116115cd576000838152602090206115cd91810190830161167c565b505050565b60206040519081016040526000815290565b60c060405190810160405280600081526020016000600160a060020a03168152602001600081526020016116166115d2565b81526020016116236115d2565b81526020016116306115d2565b905290565b50805460018160011615610100020316600290046000825580601f1061165b5750611679565b601f016020900490600052602060002090810190611679919061167c565b50565b61169691905b808211156115a55760008155600101611682565b905600a165627a7a723058208420b42cb3a4a59b7027c6d7c7546800e743bc57519a685119169f83fc5976810029";

    protected ERC725Contract(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected ERC725Contract(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public List<ClaimRequestedEventResponse> getClaimRequestedEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("ClaimRequested", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<Utf8String>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<ClaimRequestedEventResponse> responses = new ArrayList<ClaimRequestedEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            ClaimRequestedEventResponse typedResponse = new ClaimRequestedEventResponse();
            typedResponse.log = transactionReceipt.getLogs().get(valueList.indexOf(eventValues));
            typedResponse.claimId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.claimType = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.issuer = (String) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.signatureType = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.signature = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.claim = (byte[]) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.uri = (String) eventValues.getNonIndexedValues().get(3).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<ClaimRequestedEventResponse> claimRequestedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("ClaimRequested", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<Utf8String>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, ClaimRequestedEventResponse>() {
            @Override
            public ClaimRequestedEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                ClaimRequestedEventResponse typedResponse = new ClaimRequestedEventResponse();
                typedResponse.log = log;
                typedResponse.claimId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.claimType = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.issuer = (String) eventValues.getIndexedValues().get(2).getValue();
                typedResponse.signatureType = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.signature = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.claim = (byte[]) eventValues.getNonIndexedValues().get(2).getValue();
                typedResponse.uri = (String) eventValues.getNonIndexedValues().get(3).getValue();
                return typedResponse;
            }
        });
    }

    public List<ClaimAddedEventResponse> getClaimAddedEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("ClaimAdded", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<Utf8String>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<ClaimAddedEventResponse> responses = new ArrayList<ClaimAddedEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            ClaimAddedEventResponse typedResponse = new ClaimAddedEventResponse();
            typedResponse.log = transactionReceipt.getLogs().get(valueList.indexOf(eventValues));
            typedResponse.claimId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.claimType = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.issuer = (String) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.signatureType = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.signature = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.claim = (byte[]) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.uri = (String) eventValues.getNonIndexedValues().get(3).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<ClaimAddedEventResponse> claimAddedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("ClaimAdded", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<Utf8String>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, ClaimAddedEventResponse>() {
            @Override
            public ClaimAddedEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                ClaimAddedEventResponse typedResponse = new ClaimAddedEventResponse();
                typedResponse.log = log;
                typedResponse.claimId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.claimType = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.issuer = (String) eventValues.getIndexedValues().get(2).getValue();
                typedResponse.signatureType = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.signature = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.claim = (byte[]) eventValues.getNonIndexedValues().get(2).getValue();
                typedResponse.uri = (String) eventValues.getNonIndexedValues().get(3).getValue();
                return typedResponse;
            }
        });
    }

    public List<ClaimRemovedEventResponse> getClaimRemovedEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("ClaimRemoved", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<Utf8String>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<ClaimRemovedEventResponse> responses = new ArrayList<ClaimRemovedEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            ClaimRemovedEventResponse typedResponse = new ClaimRemovedEventResponse();
            typedResponse.log = transactionReceipt.getLogs().get(valueList.indexOf(eventValues));
            typedResponse.claimId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.claimType = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.issuer = (String) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.signatureType = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.signature = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.claim = (byte[]) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.uri = (String) eventValues.getNonIndexedValues().get(3).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<ClaimRemovedEventResponse> claimRemovedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("ClaimRemoved", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<Utf8String>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, ClaimRemovedEventResponse>() {
            @Override
            public ClaimRemovedEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                ClaimRemovedEventResponse typedResponse = new ClaimRemovedEventResponse();
                typedResponse.log = log;
                typedResponse.claimId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.claimType = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.issuer = (String) eventValues.getIndexedValues().get(2).getValue();
                typedResponse.signatureType = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.signature = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.claim = (byte[]) eventValues.getNonIndexedValues().get(2).getValue();
                typedResponse.uri = (String) eventValues.getNonIndexedValues().get(3).getValue();
                return typedResponse;
            }
        });
    }

    public List<ClaimChangedEventResponse> getClaimChangedEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("ClaimChanged", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<Utf8String>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<ClaimChangedEventResponse> responses = new ArrayList<ClaimChangedEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            ClaimChangedEventResponse typedResponse = new ClaimChangedEventResponse();
            typedResponse.log = transactionReceipt.getLogs().get(valueList.indexOf(eventValues));
            typedResponse.claimId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.claimType = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.issuer = (String) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.signatureType = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.signature = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.claim = (byte[]) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.uri = (String) eventValues.getNonIndexedValues().get(3).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<ClaimChangedEventResponse> claimChangedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("ClaimChanged", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<Utf8String>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, ClaimChangedEventResponse>() {
            @Override
            public ClaimChangedEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                ClaimChangedEventResponse typedResponse = new ClaimChangedEventResponse();
                typedResponse.log = log;
                typedResponse.claimId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.claimType = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.issuer = (String) eventValues.getIndexedValues().get(2).getValue();
                typedResponse.signatureType = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.signature = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.claim = (byte[]) eventValues.getNonIndexedValues().get(2).getValue();
                typedResponse.uri = (String) eventValues.getNonIndexedValues().get(3).getValue();
                return typedResponse;
            }
        });
    }

    public List<KeyAddedEventResponse> getKeyAddedEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("KeyAdded", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList());
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<KeyAddedEventResponse> responses = new ArrayList<KeyAddedEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            KeyAddedEventResponse typedResponse = new KeyAddedEventResponse();
            typedResponse.log = transactionReceipt.getLogs().get(valueList.indexOf(eventValues));
            typedResponse.key = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.keyType = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<KeyAddedEventResponse> keyAddedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("KeyAdded", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList());
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, KeyAddedEventResponse>() {
            @Override
            public KeyAddedEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                KeyAddedEventResponse typedResponse = new KeyAddedEventResponse();
                typedResponse.log = log;
                typedResponse.key = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.keyType = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public List<KeyRemovedEventResponse> getKeyRemovedEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("KeyRemoved", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList());
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<KeyRemovedEventResponse> responses = new ArrayList<KeyRemovedEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            KeyRemovedEventResponse typedResponse = new KeyRemovedEventResponse();
            typedResponse.log = transactionReceipt.getLogs().get(valueList.indexOf(eventValues));
            typedResponse.key = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.keyType = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<KeyRemovedEventResponse> keyRemovedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("KeyRemoved", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList());
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, KeyRemovedEventResponse>() {
            @Override
            public KeyRemovedEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                KeyRemovedEventResponse typedResponse = new KeyRemovedEventResponse();
                typedResponse.log = log;
                typedResponse.key = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.keyType = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public List<KeyReplacedEventResponse> getKeyReplacedEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("KeyReplaced", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList());
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<KeyReplacedEventResponse> responses = new ArrayList<KeyReplacedEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            KeyReplacedEventResponse typedResponse = new KeyReplacedEventResponse();
            typedResponse.log = transactionReceipt.getLogs().get(valueList.indexOf(eventValues));
            typedResponse.oldKey = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newKey = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.keyType = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<KeyReplacedEventResponse> keyReplacedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("KeyReplaced", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList());
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, KeyReplacedEventResponse>() {
            @Override
            public KeyReplacedEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                KeyReplacedEventResponse typedResponse = new KeyReplacedEventResponse();
                typedResponse.log = log;
                typedResponse.oldKey = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.newKey = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.keyType = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public List<ExecutionRequestedEventResponse> getExecutionRequestedEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("ExecutionRequested", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<ExecutionRequestedEventResponse> responses = new ArrayList<ExecutionRequestedEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            ExecutionRequestedEventResponse typedResponse = new ExecutionRequestedEventResponse();
            typedResponse.log = transactionReceipt.getLogs().get(valueList.indexOf(eventValues));
            typedResponse.executionId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.value = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.data = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<ExecutionRequestedEventResponse> executionRequestedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("ExecutionRequested", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, ExecutionRequestedEventResponse>() {
            @Override
            public ExecutionRequestedEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                ExecutionRequestedEventResponse typedResponse = new ExecutionRequestedEventResponse();
                typedResponse.log = log;
                typedResponse.executionId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.value = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
                typedResponse.data = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public List<ExecutedEventResponse> getExecutedEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Executed", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<ExecutedEventResponse> responses = new ArrayList<ExecutedEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            ExecutedEventResponse typedResponse = new ExecutedEventResponse();
            typedResponse.log = transactionReceipt.getLogs().get(valueList.indexOf(eventValues));
            typedResponse.executionId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.value = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.data = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<ExecutedEventResponse> executedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Executed", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, ExecutedEventResponse>() {
            @Override
            public ExecutedEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                ExecutedEventResponse typedResponse = new ExecutedEventResponse();
                typedResponse.log = log;
                typedResponse.executionId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.value = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
                typedResponse.data = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public List<ApprovedEventResponse> getApprovedEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Approved", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<ApprovedEventResponse> responses = new ArrayList<ApprovedEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            ApprovedEventResponse typedResponse = new ApprovedEventResponse();
            typedResponse.log = transactionReceipt.getLogs().get(valueList.indexOf(eventValues));
            typedResponse.executionId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.approved = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<ApprovedEventResponse> approvedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Approved", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, ApprovedEventResponse>() {
            @Override
            public ApprovedEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                ApprovedEventResponse typedResponse = new ApprovedEventResponse();
                typedResponse.log = log;
                typedResponse.executionId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.approved = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public RemoteCall<TransactionReceipt> addClaim(BigInteger _claimType, String _issuer, BigInteger _signatureType, byte[] _signature, byte[] _claim, String _uri) {
        Function function = new Function(
                "addClaim", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_claimType), 
                new org.web3j.abi.datatypes.Address(_issuer), 
                new org.web3j.abi.datatypes.generated.Uint256(_signatureType), 
                new org.web3j.abi.datatypes.DynamicBytes(_signature), 
                new org.web3j.abi.datatypes.DynamicBytes(_claim), 
                new org.web3j.abi.datatypes.Utf8String(_uri)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<List> getKeysByType(BigInteger _type) {
        Function function = new Function("getKeysByType", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_type)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Address>>() {}));
        return executeRemoteCallSingleValueReturn(function, List.class);
    }

    public RemoteCall<BigInteger> getKeyType(String _key) {
        Function function = new Function("getKeyType", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_key)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> approve(byte[] _id, Boolean _approve) {
        Function function = new Function(
                "approve", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_id), 
                new org.web3j.abi.datatypes.Bool(_approve)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> removeClaim(byte[] _claimId) {
        Function function = new Function(
                "removeClaim", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_claimId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> removeKey(String _key) {
        Function function = new Function(
                "removeKey", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_key)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<List> getClaimsIdByType(BigInteger _claimType) {
        Function function = new Function("getClaimsIdByType", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_claimType)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Bytes32>>() {}));
        return executeRemoteCallSingleValueReturn(function, List.class);
    }

    public RemoteCall<TransactionReceipt> addKey(String _key, BigInteger _type) {
        Function function = new Function(
                "addKey", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_key), 
                new org.web3j.abi.datatypes.generated.Uint256(_type)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> replaceKey(String _oldKey, String _newKey) {
        Function function = new Function(
                "replaceKey", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_oldKey), 
                new org.web3j.abi.datatypes.Address(_newKey)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> execute(String _to, BigInteger _value, byte[] _data) {
        Function function = new Function(
                "execute", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_to), 
                new org.web3j.abi.datatypes.generated.Uint256(_value), 
                new org.web3j.abi.datatypes.DynamicBytes(_data)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Tuple6<BigInteger, String, BigInteger, byte[], byte[], String>> getClaim(byte[] _claimId) {
        final Function function = new Function("getClaim", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_claimId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<DynamicBytes>() {}, new TypeReference<Utf8String>() {}));
        return new RemoteCall<Tuple6<BigInteger, String, BigInteger, byte[], byte[], String>>(
                new Callable<Tuple6<BigInteger, String, BigInteger, byte[], byte[], String>>() {
                    @Override
                    public Tuple6<BigInteger, String, BigInteger, byte[], byte[], String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);;
                        return new Tuple6<BigInteger, String, BigInteger, byte[], byte[], String>(
                                (BigInteger) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue(), 
                                (byte[]) results.get(3).getValue(), 
                                (byte[]) results.get(4).getValue(), 
                                (String) results.get(5).getValue());
                    }
                });
    }

    public static RemoteCall<ERC725Contract> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(ERC725Contract.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<ERC725Contract> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(ERC725Contract.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static ERC725Contract load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new ERC725Contract(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static ERC725Contract load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new ERC725Contract(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static class ClaimRequestedEventResponse {
        public Log log;

        public byte[] claimId;

        public BigInteger claimType;

        public String issuer;

        public BigInteger signatureType;

        public byte[] signature;

        public byte[] claim;

        public String uri;
    }

    public static class ClaimAddedEventResponse {
        public Log log;

        public byte[] claimId;

        public BigInteger claimType;

        public String issuer;

        public BigInteger signatureType;

        public byte[] signature;

        public byte[] claim;

        public String uri;
    }

    public static class ClaimRemovedEventResponse {
        public Log log;

        public byte[] claimId;

        public BigInteger claimType;

        public String issuer;

        public BigInteger signatureType;

        public byte[] signature;

        public byte[] claim;

        public String uri;
    }

    public static class ClaimChangedEventResponse {
        public Log log;

        public byte[] claimId;

        public BigInteger claimType;

        public String issuer;

        public BigInteger signatureType;

        public byte[] signature;

        public byte[] claim;

        public String uri;
    }

    public static class KeyAddedEventResponse {
        public Log log;

        public String key;

        public BigInteger keyType;
    }

    public static class KeyRemovedEventResponse {
        public Log log;

        public String key;

        public BigInteger keyType;
    }

    public static class KeyReplacedEventResponse {
        public Log log;

        public String oldKey;

        public String newKey;

        public BigInteger keyType;
    }

    public static class ExecutionRequestedEventResponse {
        public Log log;

        public byte[] executionId;

        public String to;

        public BigInteger value;

        public byte[] data;
    }

    public static class ExecutedEventResponse {
        public Log log;

        public byte[] executionId;

        public String to;

        public BigInteger value;

        public byte[] data;
    }

    public static class ApprovedEventResponse {
        public Log log;

        public byte[] executionId;

        public Boolean approved;
    }
}
