if __FILE__ == $0
  # TODO Generated stub
end


def skip_lines(file,no)
  for i in 1..no do
  file.readline
  end
end

def read_string(file, len)
	ret_val=""
	len.times do
		ret_val+=file.readline.gsub("\n","")
	
	end
	return ret_val

end

def read_header(file)
	s = read_string(file,1)
	if s!="E6" then
		puts "Wrong header start: #{s} instead of E6"
		return false
	end
	name = read_string(file,2)
	puts "File id: #{name}"
	crc1 = read_string(file,1)
	crc2 = read_string(file,1)
	check_crc="%02X"%(-crc1.to_i(16) & 0xFF)
	if crc2 != check_crc then
		print "Wrong CRC verifyer for #{crc1}: #{crc2} instead of #{check_crc}"
		return false
	end
	puts "Checksum: #{crc1}"
	start = read_string(file,2)
	puts "Start: #{start}"
	len = read_string(file,2)
	puts "Length: #{len}"
	filler = read_string(file,8)
	puts "Filler: #{filler}"
	end_mark=read_string(file,1)
	if end_mark!="E6" then
		puts "Wrong header end: #{end_mark} instead of E6"
		return false
	end
	ret_val={:len=>len.to_i(16), :start=>start.to_i(16), :crc=>crc1.to_i(16)}
	return ret_val
end	
	
	

def hex2bin(inFile, outFile, lenHeader, lenData)
counter = 0

#while counter<lenHeader
#  inFile.gets
#  counter +=1
#end
crc=0
counter = 0
arr = []
skip_lines(inFile, lenHeader)

while counter<lenData
  myByte = inFile.gets.to_i(16)
  arr[0]=myByte
  crc=(crc+myByte) & 0xFF
  outFile.write(arr.pack("C"))
  
  #if counter < 15 then
  #  print "%02X " % myByte
  #end
  #acc +=myByte
  #acc = acc & 0x00ff
  counter +=1  
end
puts ("")
#acc = (~acc + 1) &0x00ff
#puts "\n\n%04X" % acc
return crc  
end

def calc_crc (inFile, lenHeader, lenData)
  counter = 0
  acc = 0
  skip_lines(inFile, lenHeader)
while counter<lenData
  myByte = inFile.readline.to_i(16)
    if counter < 15 then
    print "%02X " % myByte
  end
  acc = (acc+myByte) & 0xff

  counter +=1  
end

acc = (acc * -1) &0xff
puts "\n\n%02X" % acc
  

end

def find_strings (inFile, lenHeader, lenData, offset)
  counter = 0
  offset -= lenHeader

  ascii_buf =""
  skip_lines(inFile, lenHeader)
while counter<lenData
  myByte = inFile.readline.to_i(16)

  counter +=1
  if myByte <=126 and myByte >=32 then
    if ascii_buf=="" then
      lStart = inFile.lineno
    end
    ascii_buf << myByte.chr
  else
    if ascii_buf.length>2 then
      puts "%04X, %04X:'%s'"%[lStart+offset,inFile.lineno-1+offset,ascii_buf]
    end
    if ascii_buf.length>0 then
      ascii_buf=""
    end
  
  end
end
end

#infName = "d:\\2009_n\\other\\tape\\prae_slow_all.hexl"
#infName = "d:\\2009_n\\other\\tape\\amic_part1.bin"
#infName = "basic.bin"
infName = ARGV[0]
outfName = infName.split(".")[0] +".ra1"
inFile = File.new(infName, "r") 
outFile = File.new(outfName,"wb")

puts "#{infName} -> #{outfName}"
#lenHeader = 18
#lenData = 0x4001
acc = 0
header = read_header(inFile)
exit -1 if header==false
crc = hex2bin(inFile,outFile, 0, header[:len])
if (crc==header[:crc]) then
	puts "CRC OK"
else
	puts "CRC verification failed. Expected #{header[:crc]}, calculated #{crc}"
end	
  #inFile.rewind
  #calc_crc(inFile, 18, 0x7000)
  #calc_crc(inFile, 3, 6)
  #find_strings(inFile, lenHeader, 0x7000, 0x7000)

inFile.close
#outFile.close
