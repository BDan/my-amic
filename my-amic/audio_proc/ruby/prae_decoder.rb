if __FILE__ == $0
  # TODO Generated stub
end

def find_block(aFile)
  state = "no_data"
  found = false
  done = false
  countZero = 0
  while not done do
    
    iChar = aFile.getc
    if iChar.nil? then
      
      done = true
      break;
    end
    aChar = iChar.chr
    if state=="no_data" then
      
      if aChar=="0" then
        #puts "got zero"
        state = "in_header"
      end
      
    elsif state=="in_header"
      #puts "in header"
      if aChar == "0" then
        countZero +=1
      elsif aChar == "1" and countZero>5 then
        found = true
        done = true
      else
        countZero=0
        state="no_data"
      end
    end
  end
  return found
end

def prae_byte(strByte)
  iByte = 0
  errMsg = nil
  if strByte.match('[^01]') then
    errMsg = "[#{@strByte}]"
  end
  iByte = strByte.reverse.to_i(2) 
  #        if iByte <=126 and iByte >=32 then
  #      print iByte.chr
  #      end
  #    retVal += "%02X" % iByte
  #    retVal
  return iByte,errMsg
  
end

class PraeDecoder
  def initialize(outFile)
    @outFile = outFile
    @len = 0
    @position = 0
    @block = 0
    @name = ""
    
    
  end
  def int_16(arr)
    return 256*arr[1]+arr[0]
  end
  
  def dump_block(block)
      block.each { |iByte| @outFile.print('%02X ' % iByte) }
      @outFile.puts "\n\n"
  end
  
  def proc_block(block)
    type = block[0]
#    checksum = block[1]
#    number = int_16 (block[2,3])
    if type==0 then
      process_header(block)
    else 
      process_data(block)
    end
  end
  
  def process_header(block)
    #puts block.inspect
    number = int_16(block[2,3])
    if number!=0 then
      puts "Processing block #{number} as a header. Error??"
      @block = number
    end
    block[6,16].each{|c| @name << c.chr}
    puts "File: #{@name}"
    @len= int_16(block[24,2])
    puts "Length: #{@len}"
  end
  
  def process_data(block)
    #puts "bleah"
    number = int_16(block[2,3])
    goodSum = block[1]
    chkSum = 0
    if number!=@block+1 then
      puts "Received block #{number} after #{@block}. Discared"
    else
      @block += 1
    end
    for i in 2..5 do
      
      chkSum = (chkSum + block[i])&0xff
    end
    block [6,128].each do |iByte|
      @outFile.puts("%02X" % iByte)
      @position += 1
      chkSum = (chkSum + iByte)&0xff
      
      if @position >= @len
        puts "Last byte found in block of type #{block[0]}. Error??" if block[0]!=0xff
        break
      end
      
    end
    chkSum = (chkSum*-1)&0xff
    if (goodSum==chkSum) then
      puts "%d: CRC OK" % @block
    else
      puts("Wrong CRC in block %d. Expected: %02X, found %02X" % [@block,goodSum, chkSum] )
      
    end
  end
end

infName = "d:\\2009_n\\other\\tape\\prae_slow_all.bit"
outfName = infName.split(".")[0] +".hexl"
inFile = File.new(infName, "r") 
outFile = File.new(outfName,"w")
fEOF = false
puts "PRAE decoder"
state = "no_block"
blockPos = 0
myDecoder = PraeDecoder.new(outFile)
while !fEOF do
  if state == "no_block" then
    if !find_block(inFile) then
      puts "EOF in header"
      fEOF = true
    else 
      
      state = "in_block"
    end
  elsif state== "in_block"
    #puts "Block"
    blockBytes=[]
    while !fEOF and blockPos<134 do
      strByte = inFile.read(8)
      
      if !strByte.nil? and strByte.length == 8
        iByte, errMsg = prae_byte(strByte)
        if !errMsg.nil?
          puts "Error at position: %d" % inFile.pos
          iByte = 0
        end
        blockBytes[blockPos]=iByte
        
        blockPos += 1        
        
      else
        puts "EOF in data block"
        puts blockPos
        fEOF == true
        break
      end
    end
    
    myDecoder.proc_block(blockBytes)
    #myDecoder.dump_block(blockBytes)

    
    blockPos = 0
    state="no_block"
    #outFile.puts "\n\n"
  else
    puts "Wrong state #{state}"
  end
end

inFile.close
outFile.close
puts "Ready"
