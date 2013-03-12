#Decodes an aMIC pulse-train file (created by wave2pulse) into hex

class ByteDecoder

  def initialize
    @strByte = ""
    @wasAsc = false
  end
  def push(strBit)
    @strByte <<strBit
    if @strByte.length > 8 then
      @strByte = @strByte[1,8]
    end
  end
  def equals?(aString)
    return aString==@strByte
  end
  def to_hex
    retVal = ""
    if @strByte.match('[^01]') then
      retVal = "[#{@strByte}]: "
    end
    #print @strByte+" "
    retVal += "%02X"%@strByte.to_i(2) #.to_s(16)
    numVal = @strByte.to_i(2)
    if numVal <126 and numVal >32 then
      print numVal.chr
      @wasAsc = true
    else
      if @wasAsc == true then
        puts ""
        @wasAsc = false
      end
    end
    return retVal
  end
  def set_byte(strByte)
    @strByte = strByte
  end
end

def get_args()
	if ARGV.length != 1 then
		puts 'Wrong argument number'
		puts 'usage: bit_decode.rb INFILE.bit'
		exit 0
	end
	return ARGV[0]
end
def run_file()
	#infName = "d:\\2009_n\\other\\tape\\amic_part2.bit"
	infName = get_args()
	outfName = infName.split(".")[0] +".bin"
	inFile = File.new(infName, "r") 
	outFile = File.new(outfName,"w")
	#headerKey = "01110000"
	headerKey = "11100110" #E6
	register = ByteDecoder.new
	begin 
	  myBit = inFile.getc
	  register.push myBit
	end while !register.equals? headerKey
	puts "got header"

	a = 0
	tempChars="00000000"
	while !tempChars.nil? and tempChars.length == 8 #a<20
	  register.set_byte tempChars  if a != 0
	  #puts register.to_hex  
	  outFile.puts register.to_hex
	  tempChars = inFile.read(8)
	  
	  a += 1
	end 
	inFile.close
	outFile.close
	puts "\nReady"
end
if __FILE__ == $0
	run_file() 
end