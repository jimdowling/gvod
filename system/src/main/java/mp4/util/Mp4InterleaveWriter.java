package mp4.util;

import java.io.DataOutputStream;
import java.io.IOException;
import mp4.util.atom.MdatAtom;
import mp4.util.atom.MoovAtom;
import mp4.util.atom.TrakAtom;


public class Mp4InterleaveWriter {

	MoovAtom moov;
	MoovAtom newMoov;
	MdatAtom mdat;
	long firstChunkOffset;
	public static final float interleaveTime = .4f;
	public Mp4InterleaveWriter(MoovAtom moov,
			MdatAtom mdat, long firstChunkOffset) {
		this.moov = moov;
		this.mdat = mdat;
		this.firstChunkOffset = firstChunkOffset;
	}
	
	public static interface InterleaveChunkHandler {

		void addChunk(int trakNum, long chunkNum, long chunkSize) throws IOException;
		
	}
	private void computeChunkPositions(InterleaveChunkHandler handler) throws IOException {
		int numTraks = moov.getTrackCount();
		long[] sampleNumber = new long [numTraks];
		long[] sampleCount = new long[numTraks];
		int curTrak = 0;
		for (int i=0;i<numTraks;i++) {
			sampleNumber[i] = 1;
			sampleCount[i] = moov.getTrack(i).getMdia().getMinf().getStbl().getStts().getTotalSampleCount();
			if (moov.getTrack(i).getMdia().getHdlr().isSound())
				curTrak = i;
		}
		long total = 0;
		float lastCts = 0;
		while(true) {
			// Find trak with lowest dts
			int minTrak = Integer.MAX_VALUE;
			float minCts = Float.MAX_VALUE;
			for (int i=0;i<numTraks;i++) {
				if (sampleNumber[i] >= sampleCount[i]) {
					if (curTrak == i)
						curTrak = -1;
					continue;
				}
				long cts = moov.getTrack(i).getMdia().getMinf().getStbl().getStts().sampleToTime(sampleNumber[i]);
				float mcts = cts / moov.getTrack(i).getMdia().getMdhd().getTimeScale();
				if (mcts < minCts) {
					minCts = mcts;
					minTrak = i;
				}
			}
			if (minTrak >= numTraks)
				break;
			if (curTrak == -1 || lastCts - minCts > interleaveTime) {
				curTrak = minTrak;
				lastCts = minCts;
			} else
				lastCts = moov.getTrack(curTrak).getMdia().getMinf().getStbl().getStts().sampleToTime(sampleNumber[curTrak]) / moov.getTrack(curTrak).getMdia().getMdhd().getTimeScale();
			// Write out chunk containing the sample
			TrakAtom trak = moov.getTrack(curTrak);
			long chunk = trak.getMdia().getMinf().getStbl().getStsc().sampleToChunk(sampleNumber[curTrak]);
			// compute the chunk size
			long sampleCnt = 0;
			long chunkSize = 0;
			for (long sample=sampleNumber[curTrak];sample <= sampleCount[curTrak] && chunk == trak.getMdia().getMinf().getStbl().getStsc().sampleToChunk(sample);sample++) {
				sampleCnt++;
				chunkSize += trak.getMdia().getMinf().getStbl().getStsz().getSampleSize(sample);
			}
			//MP4Log.log("Trak: " + minTrak + ", Chunk: " + chunk + ", Size: " + chunkSize);
			handler.addChunk(curTrak, chunk, chunkSize);
			sampleNumber[curTrak] += sampleCnt;
			total += chunkSize;
			
		}
		MP4Log.log("Total size of all mdat chunks: " + total);
	}
	public void calcInterleave() throws IOException {
		newMoov = moov.cut(0); 
		long start;
		start = System.currentTimeMillis();
		MP4Log.log("Start reinterleave...");
		computeChunkPositions(new InterleaveChunkHandler() {
			long offset = firstChunkOffset;
			public void addChunk(int trakNum, long chunkNum, long chunkSize) {
				newMoov.getTrack(trakNum).getMdia().getMinf().getStbl().getStco().setChunkOffset((int)chunkNum-1, offset);
				offset += chunkSize;
			}
		});		
		MP4Log.log("Finished reinterleave in: " + (System.currentTimeMillis() - start) / 1000.0 + "s");					
	}

	public void write(final DataOutputStream dos, boolean writeMdat) throws IOException {
		newMoov.writeData(dos);
		if (writeMdat) {
			mdat.setSize(0);
			mdat.writeHeader(dos);
			computeChunkPositions(new InterleaveChunkHandler() {
				public void addChunk(int trakNum, long chunkNum, long chunkSize) throws IOException {
					long offset = moov.getTrack(trakNum).getMdia().getMinf().getStbl().getStco().getChunkOffset((int)chunkNum);
					mdat.writeChunk(dos, offset-firstChunkOffset, chunkSize);
				}
			});
		}
	}

}
