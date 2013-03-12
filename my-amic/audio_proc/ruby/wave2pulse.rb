
@NOISE_THRESHOLD =0.03
@last_symbol="."
@phase="-" #phase is "-" for some records and "+" for others. 
def detect_pulse(pulse, sample)
	ret_val = {}
	if (pulse[:sign]==sgn(sample)) then
		ret_val[:new]=false
		ret_val[:len]=pulse[:len]+1
		ret_val[:sign]=pulse[:sign]
		ret_val[:vol]= [pulse[:vol],sample.abs].max
	else
		ret_val[:new]=true
		ret_val[:len]=1
		ret_val[:sign]=sgn(sample)
		ret_val[:vol]=sample.abs
	end
	return ret_val
end

def print_histogram(vHash)
  hist = vHash.sort {|a,b| b[0]<=>a[0]}
  hist.each do |pair|
    puts "%d\t%d" % [pair[1],pair[0]]#[hist[i,1], hist[i,0]]
  end
end



def monoAvg(sampleArr)
  sTotal = 0.0
  sampleArr.each {|sample|
    sTotal += sample
  }
  sTotal / sampleArr.length
end

def sgn(number)
  number > 0 ?1:-1
end


#def classifier2(length,table, statHash) 
def pulse_classifier(pulse, table, statHash)
  sSymbol = ""
  length = pulse[:len]
  sign = pulse[:sign]>0 ? '+':'-'
  is_noise = pulse[:vol]<@NOISE_THRESHOLD
  #puts "#{pulse[:vol]} < #{@NOISE_THRESHOLD}" if is_noise==true
  if length<table[0] then 
    sSymbol = "s"
  elsif length >= table[0] and length <table[1]
    sSymbol = "0"
  elsif length >= table[1] and length <table[2]
    sSymbol = "i"
  elsif length >= table[2] and length <table[3]
    sSymbol = "1"
  elsif length >= table[3] 
    sSymbol = "L"
  else 
    sSymbol = "?"
  end
  
  sym = sign+sSymbol
  if statHash[sym] != nil then
    statHash[sym] += 1
  else
    statHash[sym] = 1
  end
  ret_val = ""
  if !(is_noise==true) then
	if sign==@phase then
		if @last_symbol==sSymbol then
			ret_val=sSymbol
		else
			ret_val="e[#{@last_symbol},#{sSymbol}]"
		end
	else
		#ret_val="n" #[#{pulse[:vol]}]"
	end
  end

  @last_symbol=sSymbol
  #print ret_val
  return ret_val

  
end


def decode_amic(infName, outfName)
  
  histo = {}
  stats = {}
  histo_bi = {}
  samples = 0
  sign = 1
  count = 0
  lastCount = 0
  
  outFile = File.new(outfName, "w") 

  #lenTab = [6,13,16,24] #aMIC, MP3, 44100
  #@phase = "-"
  #aMIC, tape, 48000
  lenTab = [5,15,16,32] 
  @phase = phase="+"

  minVol=0
  maxVol=0
  smpCount=0
  
  buffer = [0,0,0,0]
  old_pulse = {:sign =>0, :len =>0, :vol=>0.0}
  File.open(infName, "r") do |infile|
    while (line = infile.gets)
      next if  line.include? ":"
      if (smpCount >= 10000) then
        #print "\rProcessed: #{samples}"
        print "."
        smpCount = 0
      else
        smpCount +=1
      end
      samples += 1
      chSamples = line.split(",").map{|sample| sample.strip.to_f}
      sMono = monoAvg chSamples
	  buffer.shift
	  buffer << sMono
	  filter = (buffer[0]+buffer[3]+3*(buffer[1]+buffer[2]))/8
	  pulse = detect_pulse(old_pulse, filter)
	  if pulse[:new] then
			outFile.print pulse_classifier(old_pulse, lenTab, stats)
			#if old_pulse[:sign]>0 then #and old_pulse[:vol]> @NOISE_THRESHOLD then
			if old_pulse[:vol]> @NOISE_THRESHOLD then
				len = old_pulse[:len]
				if histo_bi[len].nil? then
					histo_bi[len]=1
				else
					histo_bi[len]+=1
				end
			end
      end
	  old_pulse = pulse
    end
    puts "\nSamples: %i\n" % samples 
    totalSym = 0
    stats.each {|key, value|
      print "#{key}: #{value} "
      totalSym += value
    }
    puts ""
    #  puts "s:#{noS} i:#{noI} L:#{noL} 0:#{noZero} 1:#{noOne}"
    puts "Symbols: %d" % (totalSym)
    print_histogram(histo_bi)
  end
  outFile.close
end


def info()
	puts '========= Pulse Decoder for aMIC and PRAE =============='
	puts '= decodes audio records of home computers to HEX files ='
	puts '= used directly in emulators                           ='
	puts '= Workflow:                                            ='
	puts '= 1: wav2ascii file.wav -> file.txt                    ='
	puts '= 2: wave2pulse.rb file.txt -> file.bit                ='
	puts '= 3: decode_amic.rb file.bit -> file.bin               ='
	puts '= 4: post_decodede file.bin -> file.ra1 (binary)       ='
	puts '= 3: bin2hex file.ra1 -> file.hex (INTEL HEX)          ='
	puts '========================================================'
	puts "\n\n"
end
def run_file()
	info
	models = ["-amic", "-prae"]
	if ARGV.length < 2 or ARGV.length >3 then
	  puts "Wrong arguments number!"
	  puts "Usage: wave2pulse.rb [-amic | -prae] input.txt [output.bit]"
	  exit 0
	end	
	model = ARGV[0]
	if (!models.include? model) then
		puts "Unknown type argument: #{model}. Allowed: #{models.join(", ")}"
		exit 0
	end
	infName = ARGV[1]
	if !ARGV[2].nil? then
	  outfName = ARGV[1]
	else
	  outfName = infName.split(".")[0] +".bit"
	end
	if !File.exist?(infName) then
		puts "Input file not found: #{infName}"
		exit 0
	end
	if infName==outfName then
		puts "Output file name is same as input name"
		exit 0
	end
	puts ": #{infName} -> #{outfName}"
	if model==models[0] then
		puts ": aMIC file"
		decode_amic(infName,outfName)
	elsif  model==models[0] then
		puts ": PRAE file"
		decode_prae(infName,outfName)
	end
end

if __FILE__ == $0
	run_file() 
end