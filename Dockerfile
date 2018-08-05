FROM debian
RUN apt-get update && apt-get install -y ruby ruby-dev make gcc g++ zlib1g-dev patch
RUN echo ":ssl_verify_mode: 0" > ~/.gemrc
RUN gem install bundler
COPY Gemfile /Gemfile
RUN bundle config --global silence_root_warning 1 && bundle install && mv /root/.bundle /.bundle
EXPOSE 4000
CMD ["/root/boot.sh"]
