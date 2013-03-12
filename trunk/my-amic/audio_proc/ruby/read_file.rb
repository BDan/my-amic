##This is the original pulse decoder and classifier from 2009
## It's obsolete in 2013, kept as source for code and ideas
## Functionality is ported in wave2pulse

if __FILE__ == $0
  # TODO Generated stub
end

samples = 0
rate = 0
channels = 0

sign = 1
lastCount = 0
count = 0
iSymbol = 0
sSymbol = "b"
lastS = ""

def sgn(number)
  number > 0 ?1:-1
end

def hist(number) #hysterezis
  if number.abs < 0.1 then
    retVal = 0
  else
    retVal = sgn(number)
  end
  retVal
end
def ssgn(number)
  number > 0 ? "+" : "-"
end

def monoAvg(sampleArr)
  sTotal = 0.0
  sampleArr.each {|sample|
    sTotal += sample
  }
  sTotal / sampleArr.length
end

def monoMax(sampleArr)
  sMax = 0.0
  sampleArr.each {|sample|
    if sample.abs> sMax.abs then
      sMax = sample
    end
  }
  sMax  
end

def print_histogram(vHash)
  hist = vHash.sort {|a,b| b[0]<=>a[0]}
  hist.each do |pair|
    puts "%d\t%d" % [pair[1],pair[0]]#[hist[i,1], hist[i,0]]
  end
end

def transactor(last, crt)
  #print last+" = "+crt
  return "" if !last.include? "-"
  if last == "-1" then
    if crt =="+1" then
      "1"
    else
      "^"
    end
  elsif last == "-0" then
    if crt=="+0" then
      "0"
    else
      "v"    end
  else
    '.'
  end 
end 

def transactor2(last, crt)
  return "" if !last.include? "-"
  if crt =="+1" then
      "0"
  elsif crt == "+0" then
      "1"
  else
      "."
  end
end 

def classifier1(length, sign, table, statHash) 
  sSymbol = ""  
  if length<table[0] then 
    sSymbol="s"
  elsif length >= table[0] and length <table[1]
    sSymbol="0"
  elsif length >= table[1] and length <table[2]
    sSymbol="i"
  elsif length >= table[2] and length <table[3]
    sSymbol="1"
  elsif length >= table[3] 
    sSymbol="L"
  else 
    sSymbol = "?"
  end
  sSymbol = ssgn(sign)+sSymbol
  if statHash[sSymbol] != nil then
    statHash[sSymbol] += 1
  else
    statHash[sSymbol] = 1
  end
  return sSymbol
end


def classifier2(length,table, statHash) 
  sSymbol = ""  
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
  if statHash[sSymbol] != nil then
    statHash[sSymbol] += 1
  else
    statHash[sSymbol] = 1
  end
  return sSymbol
end

def classifier3(length, sign, table)
  retVal = "" 
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
  
  retVal = ssgn(sign) + sSymbol
  return retVal
end


def classifier4(length, sign, table)
  retVal = ""  
  if length<table[0] then 
    sSymbol = "s"
    no = 1
  elsif length >= table[0] and length <table[1]
    sSymbol = ssgn(sign)
    no=1
  elsif length >= table[1] and length <table[2]
    sSymbol = "i"
    no=1
  elsif length >= table[2] and length <table[3]
    sSymbol = ssgn(sign)
    no=2
  elsif length >= table[3] 
    sSymbol = "L"
    no=1
  else 
    sSymbol = "?"
    no=1
  end
  #sSymbol = ssgn(sign)+sSymbol
  for i in 1..no do
    retVal += sSymbol
  end
  return retVal
end


def amic_decoder(infName, outfName)
  
  histo = {}
  stats = {}
  histo_bi = {}
  
  
  outFile = File.new(outfName, "w") 
  #lenTab = [6,12,12,21] #PRAE M Fast
  #lenTab = [10,15,19,27] #PRAE M Slow
  
  #lenTab = [8,15,17,31] #aMIC
  lenTab = [18,31,41,54] #aMIC
  #lenTab = [5,11,11,19]
  #lenTab = [7,9,12,17]
  
  smpCount=0
  File.open(infName, "r") do |infile|
    #File.open("d:\\2009_n\\other\\tape\\amic_fragment1.txt", "r") do |infile|
    #File.open("d:\\2009_n\\other\\tape\\prae_block_1_filter.txt", "r") do |infile|
    #File.open("d:\\2009_n\\other\\tape\\prae_fragment2_1.txt", "r") do |infile|
    #File.open("d:\\2009_n\\other\\tape\\vis_z80_aMIC_3_hi_vol.txt", "r") do |infile|  
    
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
      #sMono = monoMax chSamples
      #sMono += 0.02 #DC bias
      
      if sgn(sMono) == sign then
        #if hist(sMono) == 0 or hist(sMono) == sign
        count +=1
        next
      end
      if (count<5) then #spike rejection
        count +=1
        sign = sgn(sMono)
        next
      end
      
      #outFile.puts "%i" % (count*sign)
      
      #    if histo[count*sign] == nil then
      #      histo[count*sign] = 1
      #    else
      #      histo[count*sign] += 1
      #    end
      
      if sgn(sMono)<0 then
        if histo_bi[count+lastCount].nil? then
          histo_bi[count+lastCount]=1
        else
          histo_bi[count+lastCount]+=1
        end
        outFile.print classifier2(count+lastCount, lenTab, stats)
      end
      #    sSymbol = classifier1(count, sign, lenTab, stats)
      #    outFile.print transactor(lastS, sSymbol)
      #    lastS=sSymbol  
      sign = sgn(sMono)
      lastCount = count
      count = 1
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
    #puts histo.inspect
  end
  outFile.close
end

$lenTab = {"prae_fast" => [6,12,17,21], 
        "prae_slow"=> [9,18,23,30],
        "amic" => [18,31,41,54],
        "test1" => [7,9,12,17]
}
def add_histogram(hashMap, value) 
  if hashMap[value].nil? then
    hashMap[value]=1
  else
    hashMap[value]+=1
  end
end

def detect_pulse(pulse, sample, ripple=5)
  retVal = nil

  #puts sgn(sample)
  if sgn(sample) == pulse[:sign] then
    pulse[:len] +=1
  else
    #puts pulse[:len]
    if (pulse[:len]<ripple) then #spike rejection
      pulse[:len] +=1
      pulse[:sign] = sgn(sample)
    else
      retVal = pulse.clone
      pulse[:len] = 1
      pulse[:sign] = sgn(sample)
    end
  end
  return retVal
end

def fm_decoder(state, input)
  retVal=""
  s1="1"
  s0="0"
  oldState = state
  if state=="x" then
    if input=="+1" then
      state = "b+"
      retVal=s0
    elsif input=="-1" then
      state = "b-"
      retVal=s0
    elsif input=="+0" then
      retVal="."
    elsif input=="-0" then
      retVal="."
    else
      retVal="E"
    end

  elsif state=="a+" then
    if input=="-0" then
      state = "b-"
      retVal=s1
    else
      retVal="E"
      state="x"      
    end


  elsif state=="a-" then
    if input=="+0" then
      state = "b+"
      retVal=s1
    else
      state="x"      
      retVal="E"
    end


  elsif state=="b+" then
    if input=="-1" then
      state = "b-"
      retVal=s0
    elsif input=="-0" then
      state = "a-"
    else
      state="x"      
      retVal="E"
    end

  elsif state=="b-" then
    if input=="+1" then
      state = "b+"
      retVal=s0
    elsif input=="+0" then
      state = "a+"
    else
      state="x"
      retVal="E"
    end

  else
  retVal ="E"
  state = "x"
end
  if retVal=="E" then
    puts oldState+" "+input
  end
return retVal, state

  
end

def manchester_decoder(state, input)
  retVal=""
  s0="0"
  s1="1"

  oldState = state
  if state=="x" then
    if input=="+1" then
      state = "a+"
      retVal=s1
    elsif input=="-1" then
      state = "a-"
      retVal=s0
    elsif input=="+0" then
      retVal="."
    elsif input=="-0" then
      retVal="."
    else
      retVal="E"
    end

  elsif state=="b+" then
    if input=="-0" then
      state = "a-"
    else
      retVal="E"
      state="x"      
    end


  elsif state=="b-" then
    if input=="+0" then
      state = "a+"
    else
      state="x"      
      retVal="E"
    end


  elsif state=="a+" then
    if input=="-1" then
      state = "a-"
      retVal=s0
    elsif input=="-0" then
      state = "b-"
      retVal=s0      
    else
      state="x"      
      retVal="E"
    end

  elsif state=="a-" then
    if input=="+1" then
      state = "a+"
      retVal=s1
    elsif input=="+0" then
      state = "b+"
      retVal=s1
    else
      state="x"
      retVal="E"
    end

  else
  retVal ="E"
  state = "x"
end
  if false #retVal=="E" then
    puts oldState+" "+input
  end
return retVal, state

  
end


def prae_decoder (infName, outfName)
  inFile = File.new(infName,"r")
  outFile = File.new(outfName, "w") 
  smpCount=0
  hPulses={}

  crtPulse = {:sign => -1, :len => 0}
  state = "x"
  while (line = inFile.gets)
    next if  line.include? ":"
    if (smpCount >= 10000) then
      print "."
      smpCount = 0
    else
      smpCount +=1
    end
    sMono = monoAvg line.split(",").map{|sample| sample.strip.to_f}
    pulse = detect_pulse(crtPulse, sMono, 0)
    if !pulse.nil? then
      #puts pulse[:sign].to_s+" "+pulse[:len].to_s
      add_histogram(hPulses, pulse[:len]*pulse[:sign])
      symbol = classifier3(pulse[:len],pulse[:sign],$lenTab["prae_slow"]) #"prae_fast"
      #msg, state = fm_decoder(state,symbol)
      msg, state = manchester_decoder(state,symbol)
      outFile.print msg
    end  
  end
  puts("")
  print_histogram(hPulses)
  outFile.close()
  inFile.close()
  
end


def run_file()
	if ARGV.length < 1 or ARGV.length >2 then
	  puts "Wrong arguments number!"
	  puts "Usage: read_file.rb input.txt [output.bit]"
	  exit 0
	end

	infName = ARGV[0]
	if !ARGV[1].nil? then
	  outfName = ARGV[1]
	else
	  outfName = infName.split(".")[0] +".bit"
	end
	puts ": #{infName} -> #{outfName}"
	amic_decoder (infName, outfName)
	#prae_decoder(infName, outfName)

end
if __FILE__ == $0
	p 'Terve!'
	run_file() 
end