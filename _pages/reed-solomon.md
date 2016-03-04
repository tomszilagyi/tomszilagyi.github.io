---
layout: page
title: "Reed-Solomon decoder IP core"
location: main-area
category: Software
permalink: /prod/reed-solomon
---

The **Reed-Solomon decoder IP core** is a highly versatile core
generator for creating hardware Reed-Solomon decoder circuits suitable
for high speed decoding of the ubiquitous Reed-Solomon Forward Error
Correction (FEC) code. This code is used in a wide variety of data
storage and transmission applications, such as the Compact Disc, DVD,
various kinds of magnetic storage, digital subscriber lines and
satellite communications.

The core generator is a Java application generating highly
comprehensible, standard, device- and technology-independent VHDL code
ready for consumption by industrial FPGA synthesis tools or ASIC
processes, while also being suitable for educational use. Generated
cores meet the requirements of various standards that specify
Reed-Solomon codes, including ATSC, CCSDS, DVB, IESS-308 and Intelsat
channel coding modes.

Decoders generated by this IP core have been extensively tested,
verified in hardware and are in active use.


## Features

- High speed, fully synchronous Reed-Solomon decoder using a single
  clock
- Implements any decoder specified via code parameters or chosen
  presets
- Supports continuous input data with no gaps between blocks
- One symbol in and out per clock cycle at any configuration
- Symbol size ranges from 3 to 12 bits; any primitive field polynomial
  usable for a given symbol size
- Supports shortened codes and erasure decoding
- Indicates errors, counts number of errors corrected and flags
  failures
- Support for marker bits useful for tagging input data, output in
  sync with output data
- User configurable generation of optional parts: VHDL for unused
  features is not generated, thus conserving resources
- Support for generating VHDL testbench matching the actual
  configuration, exercising all core features, as well as test input
  and reference output data


## Publications

The following freely downloadable publications are available for this product.

![PDF](/images/common/pdf.png) [Product datasheet]  
![PDF](/images/common/pdf.png) [Table of field polynomials]

## Availability

The Reed-Solomon decoder IP core is my sole intellectual property, and
is available for licensing. Please [get in touch] if you are interested.


## Screenshots

Representative screenshots of the core generator Java application,
showing the Factorial Consulting branding (the company was my
self-proprietorship while developing this product):

![codepar](/images/reed-solomon/codepar.png)  
Code parameters selection

![optional](/images/reed-solomon/optional.png)  
Optional core features

![pinout](/images/reed-solomon/pinout.png)  
Pinout diagram

![output](/images/reed-solomon/output.png)  
Output settings


[Product datasheet]:          /files/reed-solomon/datasheet.pdf
[Table of field polynomials]: /files/reed-solomon/fieldpolys.pdf
[get in touch]:               mailto:tomszilagyi@gmail.com