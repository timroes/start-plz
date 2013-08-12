/*
 * Copyright 2013 Tim Roes <mail@timroes.de>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.timroes.startplz.plugins;

import de.timroes.startplz.Log;
import de.timroes.startplz.Plugin;
import de.timroes.startplz.Result;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import org.a0z.mpd.MPD;
import org.a0z.mpd.MPDPlaylist;
import org.a0z.mpd.MPDServerException;
import org.a0z.mpd.Music;

/**
 * Searches the current playlist and the media library for songs.
 * 
 * @author Tim Roes <mail@timroes.de>
 */
public class MPDPlugin extends Plugin {

	private MPD mpd;
	private MPDPlaylist playlist;
	
	public MPDPlugin() {
		mpd = new MPD();
		connect();
	}
	
	/**
	 * Connect to the local mpd server.
	 */
	private void connect() {
		try {
			mpd.connect("127.0.0.1");
			playlist = mpd.getPlaylist();
		} catch (MPDServerException ex) {
			Log.w("Couldn't connect to MPD server.", ex);
		}
	}
	
	@Override
	public List<? extends Result> search(String query) {
		
		List<MPDResult> results = new LinkedList<MPDResult>();
		
		// Try to connect if we aren't connected yet.
		if(!mpd.isConnected()) {
			connect();
			if(!mpd.isConnected()) {
				return results;
			}
		}
		
		query = query.toLowerCase();
		boolean musicSearch = false;
		if(query.startsWith("m:")) {
			musicSearch = true;
			query = query.substring(2).trim();
		}
		
		searchPlaylist(query, results);
		if(query.length() > 5 || (musicSearch && query.length() > 2)) {
			// Only search whole library, when query is long enough or if 
			// user really wanted a music search by entering 'm:' in front
			// of query.
			searchMusicLibrary(query, results);
		}
		
		return results;
	}

	private void searchPlaylist(String query, List<MPDResult> results) {
		
		// Refresh playlist data from server.
		try {
			playlist.refresh();
		} catch (MPDServerException ex) {
			Log.w("Could not load playlist from MPD server.", ex);
			return;
		}

		query = query.toLowerCase();
		for(Music m : playlist.getMusics()) {
			// Search for query in artist and title of song
			if((m.getTitle() != null && m.getTitle().toLowerCase().contains(query))
						|| (m.getArtist() != null && m.getArtist().toLowerCase().contains(query))) {
				results.add(new MPDResult(m, true, getWeight(m, query)));
			}
		}
		
	}

	private void searchMusicLibrary(String query, List<MPDResult> results) {
		
		try {
			List<Music> musics = mpd.search("any", query);
			
			for(Music m : musics) {
				boolean isInPlaylist = false;
				for(MPDResult r : results) {
					if(r.music.getFilename().equals(m.getFilename())) {
						isInPlaylist = true;
						break;
					}
				}
				if(!isInPlaylist) {
					results.add(new MPDResult(m, false, getWeight(m, query)));
				}
			}
			
		} catch (MPDServerException ex) {
			Log.w("Couldn't search for songs.", ex);
		}
		
	}
	
	/**
	 * Returns the weight of a special song for a given query. This is the 
	 * weight of the highest string similarity with either the title, album, artist
	 * or filename of the song.
	 * 
	 * @param m The song.
	 * @param query The query.
	 * @return The weight of the given song for that specific query.
	 */
	private double getWeight(Music m, String query) {
		return getMaximumStringSimilarity(query, m.getTitle(), m.getAlbum(), 
				m.getArtist(), m.getFilename());
	}
	
	private class MPDResult extends Result {

		private Music music;
		private boolean inPlaylist;
		private double weight;
		
		MPDResult(Music m, boolean inPlaylist, double weight) {
			music = m;
			this.inPlaylist = inPlaylist;
			this.weight = weight;
			this.weight = weight;
		}
		
		@Override
		public String getID() {
			return music.getFilename();
		}

		@Override
		public double getWeight() {
			return weight;
		}

		@Override
		public String getTitle() {
			return music.getArtist() + " - " + music.getTitle();
		}

		@Override
		public String getSubtitle() {
			return music.getAlbum();
		}

		@Override
		public ImageIcon getIcon() {
			return createPluginIcon(inPlaylist ? "song.png" : "song-add.png");
		}

		@Override
		public void execute() {
			try {
				if(!inPlaylist) {
					playlist.add(music);
					music = playlist.getMusic(playlist.size() - 1);
				}
				mpd.skipTo(music.getSongId());
			} catch (MPDServerException ex) {
				Log.w("Error skipping to music.", ex);
			}
		}
		
	}
	
}
