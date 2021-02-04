# P2PSharing_project
University project - subject DSS 

 **TODO:**
	** General **
	* [x] send size of file (from client)
	* [x] allocate space(in server)
	* [x] make hash of a set of bytes(new class) 
	* [x] compare hashes
	* [x] download
	* [x] send hash
	* [x] send block by block and compare hash by hash
	
	** First part **
		* All *
	* [x] Select at least one file to share
	* [x] Create a summary ("torrent") file:

	** Second part **
		* All *
	* [x] Register in the tracker the “torrent” (the hashes) of the shared file (summary). So that the tracker can in its turn inform the other pairs of the available files by returning the “torrent” file.
	* [x] Register its own access information.

		* Tracker *
	* [x] Tracker allows the registration of the shared file summary (“torrent”), so that a seed can provide such information.
	* [x] Tracker Allows the registration of the access information of the peers. The peers will register with the tracker, providing the information needed to connect to them: IP / port or remote object name.
	* [x] Tracker Allows the registration of seed / download. Peers register in the tracker if they are downloading the file or acting as seeds.
	* [x] Tracker provides the summary of the file to peers who request it.
	* [x] Tracker provides a list of peers who are sharing the file. That is, it provides a list of the access information (IP/remote object name) of such peers.
	* [x] Tracker registers when a peers leaves the swarm. So that the tracker can delete it from the list.

	** Third part **
		* Peers *
	* [x] Register on the tracker.
	* [x] Inform the tracker when they leave the swarm.
	* [x] Keep a list of other participants (neighbors).
	* [ ] Exchange block information.
	* [ ] Inform the other pairs of the blocks they acquire.
	* [ ] Block Transfer.
	* [ ] Integrity check block.

		* Algorithms *
	* [x] Block selection algorithm.
	* [x] Peer selection algorithm.
