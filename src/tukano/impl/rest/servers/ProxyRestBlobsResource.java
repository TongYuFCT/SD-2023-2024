package tukano.impl.rest.servers;

import tukano.impl.api.java.ExtendedBlobs;
import tukano.impl.api.rest.RestExtendedBlobs;
import tukano.impl.proxy.DropboxService;

public class ProxyRestBlobsResource extends RestResource implements RestExtendedBlobs {

    final ExtendedBlobs impl;
	
	public ProxyRestBlobsResource() {
		this.impl = new DropboxService();
	}
	
	@Override
	public void upload(String blobId, byte[] bytes) {
		super.resultOrThrow( impl.upload(blobId, bytes));
	}

	@Override
	public byte[] download(String blobId) {
		return super.resultOrThrow( impl.download( blobId ));
	}

	@Override
	public void delete(String blobId, String token) {
		super.resultOrThrow( impl.delete( blobId, token ));
	}
	
	@Override
	public void deleteAllBlobs(String userId, String password) {
		super.resultOrThrow( impl.deleteAllBlobs( userId, password ));
	}
}
