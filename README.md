

#  **TODO:**
	* [x] send size of file (from server)
	* [x] allocate space(in client)
	* [x] make hash of a set of bytes(new class) 
	* [x] compare hashes
	* [x] download
	* [x] send hash
	* [ ] send block by block and compare hash by hash
	* [ ] encapsulation! make list of sharing files in another class(peer class or something like that)
	* [ ] Neighbor class that includes all information from peers: IP / port or remote object name, your bitmap of the blocks having, if you are downloading, the download rate if necessary, etc. .
	* [ ] define message types using classes, which will facilitate the transport network
	* [ ] run the application on the server lab (labit601.upct.es) and check that it works properly with peers running on a remote computer
	* [ ] add threads and Runnable 