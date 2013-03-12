/**** 
wav2ascii - utility program to convert audio files to ascii tables of sample values
it is used for performing DSP algorithm in languages that don't support direct access to 
binary formats.

****/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sndfile.h>

static void usage_exit (void) ;
//detect how many arguments
// 1 -> command line processing output will be name.txt (with substitution algorithm)
//2 -> output will be read from command line
// open the input and output files, read the info from input
// find no. of channels, sample rate, bit depth, format
// write file header
// read samples in batches of 1024
// print ascii values to output
//repeat until ready
//close both files

int
main (int argc, char ** argv)
{
	SNDFILE *infile;
    FILE *outfile;
	SF_INFO sfinfo ;
    char *cpInfile;
    char *cpOutfile;
    char *cpDot;
    int channels;
    float * readBuf;
    const int readLen = 2048;
    sf_count_t noSamples;
    int i,j, total=0;

	if (argc < 2 || argc > 3)
		usage_exit () ;

    cpInfile = argv[1];
    if (argc == 3) {
	if (strcmp (argv [argc - 2], argv [argc - 1]) == 0)
	{	printf ("Error : input and output file names are the same.\n") ;
		exit (1) ;
		} ;
    cpOutfile = argv[2];
    } else{
        cpOutfile = malloc (strlen(cpInfile)+4);
        strcpy (cpOutfile, cpInfile);
        cpDot = strrchr(cpOutfile,'.');
        if (cpDot != NULL) {
            *cpDot = 0;
        }
        strcat (cpOutfile,".txt");
    }

	/* Delete the output file length to zero if already exists. */
	remove (cpOutfile) ;

	memset (&sfinfo, 0, sizeof (sfinfo)) ;
	if ((infile = sf_open (cpInfile, SFM_READ, &sfinfo)) == NULL)
	{	printf ("Error : Not able to open input file '%s'\n", cpInfile) ;
		sf_close (infile) ;
		exit (1) ;
		} ;

    channels = sfinfo.channels;
    readBuf = malloc(channels*readLen*sizeof(float));
    outfile = fopen(cpOutfile, "w");
    printf("Processing %s into %s\n", cpInfile, cpOutfile);    
    fprintf (outfile,"Samples: %I64d\nChannels: %d\nSample rate: %d\nFormat: %x\n", sfinfo.frames, channels, sfinfo.samplerate, sfinfo.format);
    printf ("Frames: %I64d\n",sfinfo.frames);
    
    
    do {
    noSamples = sf_readf_float(infile,readBuf,readLen);
    printf (".");
    total += noSamples;
    for (i=0; i<noSamples; i++){
    for (j=0; j<channels;j++) {
        fprintf(outfile,"%f",readBuf[channels*i+j]);
        if (j<channels-1){
            fprintf(outfile,", ");
        }
    }
    fprintf(outfile,"\n");
    
    
    }
    } while (noSamples == readLen);
    printf ("\nReady\nTotal: %d", total);
    fclose (outfile);
	sf_close (infile) ;

	return 0 ;
}

static void usage_exit (void)
{
	puts ("\n"
		"Usage :\n\n"
		"    wav2ascii <input file> [output file]\n"
        "If 'output file' is not specified, '<input file>.txt' will be used instead."
		) ;
	exit (0);
} /* usage_exit */
